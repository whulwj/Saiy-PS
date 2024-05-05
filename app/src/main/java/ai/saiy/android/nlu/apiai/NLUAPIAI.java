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

package ai.saiy.android.nlu.apiai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.protobuf.ListValue;
import com.google.protobuf.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by benrandall76@gmail.com on 03/06/2016.
 */
public class NLUAPIAI {

    private final ArrayList<String> results;
    private final float[] confidence;
    private final String intent;
    private final HashMap<String, JsonElement> parameters;

    public NLUAPIAI(@NonNull final float[] confidence, @NonNull final ArrayList<String> results,
                    @NonNull final String intent, @Nullable final Map<String, Value> parameters) {
        this.confidence = confidence;
        this.results = results;
        this.intent = intent;
        if (parameters == null) {
            this.parameters = new HashMap<>();
        } else {
            this.parameters = new HashMap<>(parameters.size());
            for (Map.Entry<String, Value> entry : parameters.entrySet()) {
                this.parameters.put(entry.getKey(), convertToJson(entry.getValue()));
            }
        }
    }

    private JsonElement convertToJson(Value value) {
        if (value.hasNullValue()) {
            return JsonNull.INSTANCE;
        } else if (value.hasNumberValue()) {
            return new JsonPrimitive(value.getNumberValue());
        } else if (value.hasStringValue()) {
            return new JsonPrimitive(value.getStringValue());
        } else if (value.hasBoolValue()) {
            return new JsonPrimitive(value.getBoolValue());
        } else if (value.hasStructValue()) {
            return convertToJson(value.getStructValue().getFieldsMap());
        } else if (value.hasListValue()) {
            final ListValue listValue = value.getListValue();
            final JsonArray jsonArray = new JsonArray(listValue.getValuesCount());
            for (Value elementValue: listValue.getValuesList()) {
                jsonArray.add(convertToJson(elementValue));
            }
            return jsonArray;
        }
        return JsonNull.INSTANCE;
    }

    private @NonNull JsonElement convertToJson(final Map<String, Value> parameters) {
        if (parameters == null) {
            return JsonNull.INSTANCE;
        }
        final JsonObject jsonObject = new JsonObject();
        for (Map.Entry<String, Value> entry: parameters.entrySet()) {
            jsonObject.add(entry.getKey(), convertToJson(entry.getValue()));
        }
        return jsonObject;
    }

    public float[] getConfidence() {
        return confidence;
    }

    public ArrayList<String> getResults() {
        return results;
    }

    public String getIntent() {
        return intent;
    }

    public HashMap<String, JsonElement> getParameters() {
        return parameters;
    }
}
