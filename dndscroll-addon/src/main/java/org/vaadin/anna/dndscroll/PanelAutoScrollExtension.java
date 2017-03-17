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

package org.vaadin.anna.dndscroll;

import com.vaadin.server.AbstractExtension;
import com.vaadin.ui.Panel;

/**
 * Extension for enabling a Panel to scroll automatically when a dragged item is
 * hovered near the top or bottom of the visible area.
 *
 * @author Teppo Kurki, Anna Koskinen / Vaadin Ltd.
 */
public class PanelAutoScrollExtension extends AbstractExtension {

    public void extend(Panel target) {
        super.extend(target);
    }
}
