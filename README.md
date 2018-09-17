[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/dndscroll-add-on)
[![Stars on vaadin.com/directory](https://img.shields.io/vaadin-directory/star/dndscroll-add-on.svg)](https://vaadin.com/directory/component/dndscroll-add-on)

# DNDScroll Add-on for Vaadin 7

DNDScroll is an extension add-on for Vaadin 7. It contains TableAutoScrollExtension and PanelAutoScrollExtension, which both add automatic scrolling when a dragged element is hovered near the end or the beginning of the layout.

NOTE: this extension replaces the default implementations of VDragAndDropManager and VDragAndDropWrapper with extended custom versions to enable the autoscroll functionality. If you have any other custom implementations of these classes in your project, you'll need to combine them manually.

For an example of how to use the extensions, see [src/main/java/org/vaadin/anna/dndscroll/demo/DemoUI.java](https://github.com/Ansku/DNDScroll/blob/master/dndscroll-demo/src/main/java/org/vaadin/anna/dndscroll/demo/DemoUI.java)
