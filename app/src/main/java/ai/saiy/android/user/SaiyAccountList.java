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

package ai.saiy.android.user;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ai.saiy.android.utils.UtilsList;

/**
 * Created by benrandall76@gmail.com on 09/09/2016.
 */

public class SaiyAccountList {
    @SerializedName("accountList")
    private final List<SaiyAccount> saiyAccountList;

    public SaiyAccountList(final List<SaiyAccount> saiyAccountList) {
        this.saiyAccountList = saiyAccountList;
    }

    public List<SaiyAccount> getSaiyAccountList() {
        return saiyAccountList;
    }

    public int size() {
        if (UtilsList.notNaked(saiyAccountList)) {
            return saiyAccountList.size();
        } else {
            return 0;
        }
    }
}