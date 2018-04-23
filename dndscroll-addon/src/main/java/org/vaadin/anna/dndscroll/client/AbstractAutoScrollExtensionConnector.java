/*
 * Copyright 2000-2017 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.vaadin.anna.dndscroll.client;

import com.google.gwt.animation.client.AnimationScheduler;
import com.google.gwt.animation.client.AnimationScheduler.AnimationCallback;
import com.google.gwt.animation.client.AnimationScheduler.AnimationHandle;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.WidgetUtil;
import com.vaadin.client.extensions.AbstractExtensionConnector;
import com.vaadin.client.ui.dd.VDragAndDropManager;
import com.vaadin.client.ui.dd.VTransferable;

/**
 * Abstract connector implementation for enabling scrolling when hovering the
 * dragged item near the end of the visible, scrollable area.
 *
 * @author Teppo Kurki, Anna Koskinen / Vaadin Ltd.
 */
public abstract class AbstractAutoScrollExtensionConnector
        extends AbstractExtensionConnector
        implements CustomDragAndDropManager.DragStartOrEndHandler {

    private static final int SCROLL_TOP_SPEED_PX_SEC = 800;
    private static final int MIN_NO_AUTOSCROLL_AREA_PX = 50;
    private int scrollAreaPX = 100;

    private HandlerRegistration dragEventsHandler;
    private HandlerRegistration handlerRegistration;
    private AutoScrollingFrame horizontalAutoScroller;
    private AutoScrollingFrame verticalAutoScroller;

    protected Element element;
    protected Element scrollable;

    private int pageY;
    private int pageX;
    private int startingBound;
    private int endingBound;
    private int gradientArea;
    protected ComponentConnector connector;
    protected Widget widget;

    public abstract Element getScrollTarget();

    @Override
    protected void extend(ServerConnector target) {
        connector = (ComponentConnector) target;
        widget = connector.getWidget();
        element = widget.getElement();

        CustomDragAndDropManager manager = (CustomDragAndDropManager) VDragAndDropManager
                .get();
        dragEventsHandler = manager.addHandler(
                CustomDragAndDropManager.DragStartOrEndEvent.TYPE, this);
        MouseUpHandler mouseUpHandler = new MouseUpHandler() {
            @Override
            public void onMouseUp(MouseUpEvent event) {
                stopAndCleanup();
            }
        };
        widget.addDomHandler(mouseUpHandler, MouseUpEvent.getType());
    }

    @Override
    public void onDragStarted(VTransferable transferable,
            NativeEvent startEvent) {
        stopAndCleanup();

        handlerRegistration = Event
                .addNativePreviewHandler(new NativePreviewHandler() {

                    @Override
                    public void onPreviewNativeEvent(NativePreviewEvent event) {
                        NativeEvent nativeEvent = event.getNativeEvent();
                        if (Event.ONMOUSEMOVE == event.getTypeInt()
                                && NativeEvent.BUTTON_LEFT == nativeEvent
                                        .getButton()) {
                            if (scrollable == null) {
                                scrollable = getScrollTarget();
                            }
                            if (horizontalAutoScroller == null
                                    && verticalAutoScroller == null) {
                                startAutoScroller(nativeEvent);
                            } else {
                                updateAutoScroller(nativeEvent);
                            }
                        }
                    }

                });
    }

    private void startAutoScroller(NativeEvent nativeEvent) {
        updateScrollBounds(ScrollAxis.HORIZONTAL);
        horizontalAutoScroller = new AutoScrollingFrame(ScrollAxis.HORIZONTAL,
                startingBound, endingBound, gradientArea);
        horizontalAutoScroller.start();
        updateScrollBounds(ScrollAxis.VERTICAL);
        verticalAutoScroller = new AutoScrollingFrame(ScrollAxis.VERTICAL,
                startingBound, endingBound, gradientArea);
        verticalAutoScroller.start();
    }

    private void updateAutoScroller(NativeEvent nativeEvent) {
        pageY = WidgetUtil.getTouchOrMouseClientY(nativeEvent);
        pageX = WidgetUtil.getTouchOrMouseClientX(nativeEvent);
        horizontalAutoScroller.updatePointerCoords(pageX, pageY);
        verticalAutoScroller.updatePointerCoords(pageX, pageY);
    }

    @Override
    public void onDragEnded() {
        stopAndCleanup();
    }

    private void stopAndCleanup() {
        if (horizontalAutoScroller != null) {
            horizontalAutoScroller.stop();
            horizontalAutoScroller = null;
        }
        if (verticalAutoScroller != null) {
            verticalAutoScroller.stop();
            verticalAutoScroller = null;
        }
        if (handlerRegistration != null) {
            handlerRegistration.removeHandler();
            handlerRegistration = null;
        }
    }

    @Override
    public void onUnregister() {
        super.onUnregister();
        stopAndCleanup();
        if (dragEventsHandler != null) {
            dragEventsHandler.removeHandler();
            dragEventsHandler = null;
        }
    }

    public enum ScrollAxis {
        VERTICAL, HORIZONTAL
    }

    private class AutoScrollingFrame implements AnimationCallback {

        private static final int GRADIENT_MIN_THRESHOLD_PX = 10;
        private static final int SCROLL_AREA_REBOUND_PX_PER_SEC = 1;
        private static final double SCROLL_AREA_REBOUND_PX_PER_MS = SCROLL_AREA_REBOUND_PX_PER_SEC
                / 1000.0d;

        private int startBound = -1;
        private int endBound = -1;
        private final int gradientArea;
        private double scrollSpeed = 0;
        private double prevTimestamp = 0;
        private double pixelsToScroll = 0.0d;
        private boolean running = false;
        private AnimationHandle handle;
        private int scrollingAxisPageCoordinate;
        private int finalStartBound;
        private int finalEndBound;
        private boolean scrollAreaShouldRebound = false;
        private ScrollAxis scrollAxis;
        private int pageX;
        private int pageY;

        public AutoScrollingFrame(ScrollAxis scrollAxis, final int startBound,
                final int endBound, final int gradientArea) {
            this.scrollAxis = scrollAxis;
            finalStartBound = startBound;
            finalEndBound = endBound;
            this.gradientArea = gradientArea;
        }

        @Override
        public void execute(final double timestamp) {
            final double timeDiff = timestamp - prevTimestamp;
            prevTimestamp = timestamp;

            reboundScrollArea(timeDiff);
            pixelsToScroll += scrollSpeed * (timeDiff / 1000.0d);
            final int intPixelsToScroll = (int) pixelsToScroll;
            pixelsToScroll -= intPixelsToScroll;
            if (intPixelsToScroll != 0) {
                double scrollPos = scrollAxis == ScrollAxis.VERTICAL
                        ? scrollable.getScrollTop()
                        : scrollable.getScrollLeft();
                double maxScrollPos = scrollAxis == ScrollAxis.VERTICAL
                        ? scrollable.getScrollHeight()
                                - scrollable.getOffsetHeight()
                        : scrollable.getScrollWidth()
                                - scrollable.getOffsetWidth();
                if (intPixelsToScroll > 0 && scrollPos < maxScrollPos
                        || intPixelsToScroll < 0 && scrollPos > 0) {
                    if (ScrollAxis.VERTICAL == scrollAxis) {
                        scrollable.setScrollTop(
                                (int) (scrollPos + intPixelsToScroll));
                    } else {
                        scrollable.setScrollLeft(
                                (int) (scrollPos + intPixelsToScroll));
                    }
                }
            }
            reschedule();
        }

        private void reboundScrollArea(double timeDiff) {
            if (!scrollAreaShouldRebound) {
                return;
            }

            int reboundPx = (int) Math
                    .ceil(SCROLL_AREA_REBOUND_PX_PER_MS * timeDiff);
            if (startBound < finalStartBound) {
                startBound += reboundPx;
                startBound = Math.min(startBound, finalStartBound);
                updateScrollSpeed(scrollingAxisPageCoordinate);
            } else if (endBound > finalEndBound) {
                endBound -= reboundPx;
                endBound = Math.max(endBound, finalEndBound);
                updateScrollSpeed(scrollingAxisPageCoordinate);
            }
        }

        private void updateScrollSpeed(final int pointerPageCordinate) {
            final double ratio;
            if (pageY < scrollable.getAbsoluteTop()
                    || pageY > scrollable.getAbsoluteBottom()
                    || pageX < scrollable.getAbsoluteLeft()
                    || pageX > scrollable.getAbsoluteRight()) {
                ratio = 0;
            } else if (pointerPageCordinate < startBound) {
                final double distance = pointerPageCordinate - startBound;
                ratio = Math.max(-1, distance / gradientArea);
            } else if (pointerPageCordinate > endBound) {
                final double distance = pointerPageCordinate - endBound;
                ratio = Math.min(1, distance / gradientArea);
            } else {
                ratio = 0;
            }

            scrollSpeed = ratio * SCROLL_TOP_SPEED_PX_SEC;
        }

        public void start() {
            running = true;
            reschedule();
        }

        public void stop() {
            running = false;

            if (handle != null) {
                handle.cancel();
                handle = null;
            }
        }

        private void reschedule() {
            if (running && gradientArea >= GRADIENT_MIN_THRESHOLD_PX) {
                handle = AnimationScheduler.get().requestAnimationFrame(this,
                        scrollable);
            }
        }

        public void updatePointerCoords(int pageX, int pageY) {
            this.pageX = pageX;
            this.pageY = pageY;
            final int pageCordinate = scrollAxis == ScrollAxis.VERTICAL ? pageY
                    : pageX;
            doScrollAreaChecks(pageCordinate);
            updateScrollSpeed(pageCordinate);
            scrollingAxisPageCoordinate = pageCordinate;
        }

        private void doScrollAreaChecks(int pageCordinate) {
            if (startBound == -1) {
                startBound = Math.min(finalStartBound, pageCordinate);
                endBound = Math.max(finalEndBound, pageCordinate);
            } else {
                int oldTopBound = startBound;
                if (startBound < finalStartBound) {
                    startBound = Math.max(startBound,
                            Math.min(finalStartBound, pageCordinate));
                }

                int oldBottomBound = endBound;
                if (endBound > finalEndBound) {
                    endBound = Math.min(endBound,
                            Math.max(finalEndBound, pageCordinate));
                }

                final boolean startDidNotMove = oldTopBound == startBound;
                final boolean endDidNotMove = oldBottomBound == endBound;
                final boolean wasMovement = pageCordinate != scrollingAxisPageCoordinate;
                scrollAreaShouldRebound = (startDidNotMove && endDidNotMove
                        && wasMovement);
            }
        }
    }

    private void updateScrollBounds(ScrollAxis scrollAxis) {
        if (ScrollAxis.VERTICAL == scrollAxis) {
            startingBound = element.getAbsoluteTop() + 1 + scrollAreaPX;
            endingBound = element.getAbsoluteBottom() - 1 - scrollAreaPX;
        } else {
            startingBound = element.getAbsoluteLeft() + 1 + scrollAreaPX;
            endingBound = element.getAbsoluteRight() - 1 - scrollAreaPX;
        }
        gradientArea = scrollAreaPX;

        if (endingBound - startingBound < MIN_NO_AUTOSCROLL_AREA_PX) {
            double adjustment = MIN_NO_AUTOSCROLL_AREA_PX
                    - (endingBound - startingBound);
            startingBound -= adjustment / 2;
            endingBound += adjustment / 2;
            gradientArea -= adjustment / 2;
        }
    }
}