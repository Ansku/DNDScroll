/*
 * Copyright 2000-2016 Vaadin Ltd.
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

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.event.shared.SimpleEventBus;
import com.vaadin.client.ui.dd.VDragAndDropManager;
import com.vaadin.client.ui.dd.VDragEvent;
import com.vaadin.client.ui.dd.VTransferable;

/**
 * Custom implementation for VDragAndDropManager with extra handling for drag
 * events.
 *
 * @author Teppo Kurki, Anna Koskinen / Vaadin Ltd.
 */
public class CustomDragAndDropManager extends VDragAndDropManager implements
        HasHandlers {

    private EventBus eventBus = GWT.create(SimpleEventBus.class);

    @Override
    public VDragEvent startDrag(VTransferable transferable,
            NativeEvent startEvent, boolean handleDragEvents) {
        VDragEvent sd = super.startDrag(transferable, startEvent, handleDragEvents);
        fireEvent(new DragStartOrEndEvent(transferable, startEvent));
        return sd;
    }

    @Override
    public void endDrag() {
        fireEvent(new DragStartOrEndEvent());
        super.endDrag();
    }

    @Override
    public void interruptDrag() {
        fireEvent(new DragStartOrEndEvent());
        super.interruptDrag();
    }

    public interface DragStartOrEndHandler extends EventHandler {
        void onDragStarted(VTransferable transferable, NativeEvent startEvent);

        void onDragEnded();
    }

    public static class DragStartOrEndEvent extends
            GwtEvent<DragStartOrEndHandler> {
        public static Type<DragStartOrEndHandler> TYPE = new Type<DragStartOrEndHandler>();
        private VTransferable transferable;
        private NativeEvent startEvent;
        private boolean dragStarted;

        public DragStartOrEndEvent(VTransferable transferable,
                NativeEvent startEvent) {
            this.transferable = transferable;
            this.startEvent = startEvent;
            dragStarted = true;
        }

        public DragStartOrEndEvent() {
            dragStarted = false;
        }

        @Override
        public com.google.gwt.event.shared.GwtEvent.Type<DragStartOrEndHandler> getAssociatedType() {
            return TYPE;
        }

        @Override
        protected void dispatch(DragStartOrEndHandler handler) {
            if (dragStarted) {
                handler.onDragStarted(transferable, startEvent);
            } else {
                handler.onDragEnded();
            }
        }
    }

    public <H extends EventHandler> HandlerRegistration addHandler(
            GwtEvent.Type<H> type, H handler) {
        return eventBus.addHandler(type, handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        eventBus.fireEvent(event);
    }
}
