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

package org.vaadin.anna.dndscroll.demo;

import javax.servlet.annotation.WebServlet;

import org.vaadin.anna.dndscroll.PanelAutoScrollExtension;
import org.vaadin.anna.dndscroll.TableAutoScrollExtension;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.dd.HorizontalDropLocation;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractOrderedLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.DragStartMode;
import com.vaadin.ui.DragAndDropWrapper.WrapperTargetDetails;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.data.util.BeanItemContainer;
import com.vaadin.v7.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.Table.TableDragMode;
import com.vaadin.v7.ui.Table.TableTransferable;

@Theme("demo")
@Title("DNDScroll Add-on Demo")
@SuppressWarnings({ "serial", "deprecation" })
@Widgetset("org.vaadin.anna.dndscroll.demo.DNDScrollDemoWidgetset")
public class DemoUI extends UI {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DemoUI.class)
    public static class Servlet extends VaadinServlet {
    }

    @Override
    protected void init(VaadinRequest request) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setStyleName("demoContentLayout");
        layout.setSizeUndefined();
        layout.setSpacing(true);
        layout.setMargin(true);
        layout.addComponents(createTable(), createHorizontalPanel(),
                createVerticalPanel());
        setContent(layout);
    }

    protected Table createTable() {
        Table table = new Table();
        BeanItemContainer<TestBean> container = new BeanItemContainer<TestBean>(
                TestBean.class);
        for (int i = 0; i < 30; ++i) {
            container.addBean(new TestBean("item" + i));
        }
        table.setDragMode(TableDragMode.ROW);
        table.setContainerDataSource(container);
        table.setPageLength(6);
        table.setWidth(200, Unit.PIXELS);
        table.setDropHandler(new TableDropHandler());
        TableAutoScrollExtension extension = new TableAutoScrollExtension();
        extension.extend(table);
        return table;
    }

    private Component createHorizontalPanel() {
        Panel panel = new Panel();
        HorizontalLayout content = new HorizontalLayout();
        content.setSpacing(true);
        PanelDropHandler dropHandler = new PanelDropHandler(content);
        for (int i = 0; i < 30; ++i) {
            Label label = new Label("label" + i);
            label.setWidthUndefined();
            DragAndDropWrapper wrapper = new DragAndDropWrapper(label);
            wrapper.setDragStartMode(DragStartMode.COMPONENT);
            wrapper.setDropHandler(dropHandler);
            content.addComponent(wrapper);
        }
        panel.setContent(content);
        panel.setWidth(200, Unit.PIXELS);
        PanelAutoScrollExtension extension = new PanelAutoScrollExtension();
        extension.extend(panel);
        return panel;
    }

    private Component createVerticalPanel() {
        Panel panel = new Panel();
        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        PanelDropHandler dropHandler = new PanelDropHandler(content);
        for (int i = 0; i < 30; ++i) {
            Label label = new Label("label" + i);
            label.setWidthUndefined();
            DragAndDropWrapper wrapper = new DragAndDropWrapper(label);
            wrapper.setDragStartMode(DragStartMode.COMPONENT);
            wrapper.setDropHandler(dropHandler);
            content.addComponent(wrapper);
        }
        panel.setContent(content);
        panel.setHeight(200, Unit.PIXELS);
        PanelAutoScrollExtension extension = new PanelAutoScrollExtension();
        extension.extend(panel);
        return panel;
    }

    protected static class PanelDropHandler implements DropHandler {
        private AbstractOrderedLayout content;

        public PanelDropHandler(AbstractOrderedLayout content) {
            this.content = content;
        }

        @Override
        public void drop(DragAndDropEvent event) {
            WrapperTargetDetails details = (WrapperTargetDetails) event
                    .getTargetDetails();
            if (!(event.getTransferable() instanceof WrapperTransferable)) {
                Notification.show("illegal drop");
                return;
            }
            WrapperTransferable transferable = (WrapperTransferable) event
                    .getTransferable();

            Component target = (details.getTarget());
            while (!(target instanceof DragAndDropWrapper)
                    && target.getParent() != null) {
                if (target == content) {
                    break;
                }
                target = target.getParent();
            }

            Component draggedComponent = transferable.getDraggedComponent();
            Component parent = draggedComponent.getParent();
            while (!content.equals(parent) && parent != null) {
                draggedComponent = parent;
                parent = parent.getParent();
            }
            if (!content.equals(parent)) {
                Notification.show("illegal drop");
                return;
            }

            content.removeComponent(draggedComponent);
            if (VerticalDropLocation.TOP.equals(details
                    .getVerticalDropLocation())
                    || HorizontalDropLocation.LEFT.equals(details
                            .getHorizontalDropLocation())) {
                content.addComponent(draggedComponent,
                        content.getComponentIndex(target));
            } else {
                content.addComponent(draggedComponent,
                        content.getComponentIndex(target) + 1);
            }
        }

        @Override
        public AcceptCriterion getAcceptCriterion() {
            return AcceptAll.get();
        }

    }

    protected static class TableDropHandler implements DropHandler {

        public TableDropHandler() {
        }

        @Override
        public void drop(DragAndDropEvent event) {
            AbstractSelectTargetDetails details = (AbstractSelectTargetDetails) event
                    .getTargetDetails();
            TableTransferable transferable = (TableTransferable) event
                    .getTransferable();

            Table table = (Table) details.getTarget();

            Object itemId = transferable.getItemId();
            table.removeItem(itemId);
            if (VerticalDropLocation.TOP.equals(details.getDropLocation())) {
                @SuppressWarnings("unchecked")
                BeanItemContainer<TestBean> container = (BeanItemContainer<TestBean>) table
                        .getContainerDataSource();
                TestBean prevItemId = container.prevItemId(details
                        .getItemIdOver());
                if (prevItemId != null) {
                    container.addItemAfter(prevItemId, itemId);
                } else {
                    container.addItemAt(0, itemId);
                }
            } else {
                table.addItemAfter(details.getItemIdOver(), itemId);
            }
        }

        @Override
        public AcceptCriterion getAcceptCriterion() {
            return AcceptAll.get();
        }

    }

    public static class TestBean {
        private String name;

        public TestBean(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
}
