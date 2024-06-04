/*
 * Copyright (c) 2016. Saiy Ltd. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ai.saiy.android.ui.containers;

import androidx.annotation.NonNull;

/**
 * Class for the simple UI list elements
 * <p>
 * Created by benrandall76@gmail.com on 18/07/2016.
 */

public class SimpleContainerUI {

    private String title;
    private String content;

    public SimpleContainerUI() {
    }

    /**
     * Constructor
     *
     * @param title     of the element
     * @param content  of the element
     */
    public SimpleContainerUI(@NonNull final String title, @NonNull final String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull final String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(@NonNull final String content) {
        this.content = content;
    }
}
