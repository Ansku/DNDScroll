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

import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.vaadin.client.ui.VDragAndDropWrapper;
import com.vaadin.client.ui.dd.VDragAndDropManager;

/**
 * Custom implementation for VDragAndDropWrapper with extra handling for drag
 * events.
 *
 * @author Teppo Kurki, Anna Koskinen / Vaadin Ltd.
 */
public class CustomDragAndDropWrapper extends VDragAndDropWrapper {
    private HandlerRegistration nativeHandlerRegistration;

    public CustomDragAndDropWrapper() {
        super();
        addDomHandler(new MouseDownHandler() {
            @Override
            public void onMouseDown(MouseDownEvent mouseDownEvent) {
                cleanUp();
                nativeHandlerRegistration = Event.addNativePreviewHandler(new Event.NativePreviewHandler() {
                    @Override
                    public void onPreviewNativeEvent(
                            Event.NativePreviewEvent nativePreviewEvent) {
                        if (Event.ONMOUSEUP == nativePreviewEvent.getTypeInt()) {
                            VDragAndDropManager.get().endDrag();
                            cleanUp();
                        }
                    }
                });
            }
        }, MouseDownEvent.getType());
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        cleanUp();
    }

    private void cleanUp() {
        if (nativeHandlerRegistration != null) {
            nativeHandlerRegistration.removeHandler();
            nativeHandlerRegistration = null;
        }
    }
}