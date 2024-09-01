package ai.saiy.android.amazon.resolve;

import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import ai.saiy.android.amazon.UtilsNetwork;
import ai.saiy.android.amazon.directives.Directive;
import ai.saiy.android.amazon.directives.DirectiveType;
import ai.saiy.android.amazon.directives.Header;
import ai.saiy.android.amazon.directives.Payload;
import ai.saiy.android.amazon.directives.StructuralDirective;
import ai.saiy.android.utils.Constants;
import ai.saiy.android.utils.MyLog;
import ai.saiy.android.utils.UtilsList;
import ai.saiy.android.utils.UtilsString;
import okhttp3.Response;

public class ResolveAmazon {
    private final InputStream inputStream;
    private final Response response;
    private final File file;

    private static final boolean DEBUG = MyLog.DEBUG;
    private final String CLS_NAME = ResolveAmazon.class.getSimpleName();
    private final String boundaryIdentifier = getBoundaryIdentifier();

    public ResolveAmazon(InputStream inputStream, Response response, File file) {
        this.inputStream = inputStream;
        this.response = response;
        this.file = file;
    }

    private String getBoundaryIdentifier() {
        String boundaryIdentifier = StringUtils.substringBetween(response.headers().get(UtilsNetwork.CONTENT_TYPE), "boundary=", ";");
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getBoundaryIdentifier: " + boundaryIdentifier);
        }
        return UtilsString.notNaked(boundaryIdentifier) ? boundaryIdentifier : "";
    }

    private String getMultipartContentId(String str) {
        String contentId = StringUtils.substringBetween(str, "Content-ID: <", ">");
        if (DEBUG) {
            MyLog.i(CLS_NAME, "getMultipartContentId: " + contentId);
        }
        return UtilsString.notNaked(contentId) ? contentId : "";
    }

    private String readHeaders(MultipartStream multipartStream) {
        try {
            return multipartStream.readHeaders();
        } catch (MultipartStream.MalformedStreamException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "readHeaders: MalformedStreamException" + ", " + e.getMessage());
            }
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "readHeaders: " + e.getClass().getSimpleName() + ", " + e.getMessage());
            }
        }
        return null;
    }

    public DirectiveList parse() throws IOException, JSONException {
        if (DEBUG) {
            MyLog.i(CLS_NAME, "parse");
        }
        long nanoTime = System.nanoTime();
        DirectiveList directiveList = new DirectiveList();
        ArrayList<StructuralDirective> arrayList = new ArrayList<>();
        ArrayList<Pair<String, byte[]>> multiPartList = new ArrayList<>();
        try {
            byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(inputStream);
            final String responseString = getString(bytes);
            if (DEBUG) {
                MyLog.i(CLS_NAME, "parse: responseToString: " + responseString);
            }
            MultipartStream multipartStream = new MultipartStream(new ByteArrayInputStream(bytes), boundaryIdentifier.getBytes(), 512, null);
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            if (multipartStream.skipPreamble()) {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "parse: initial boundary: true");
                }
                boolean isFirst = true;
                while (true) {
                    if (!isFirst && !multipartStream.readBoundary()) {
                        break;
                    }
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "parse: first: " + isFirst);
                    }
                    String headers = readHeaders(multipartStream);
                    if (!UtilsString.notNaked(headers)) {
                        break;
                    }
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "headers:\n" + headers);
                    }
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    multipartStream.readBodyData(byteArrayOutputStream);
                    if (headers.contains(UtilsNetwork.JSON_UTF_8)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "parse: json");
                        }
                        String directiveString = byteArrayOutputStream.toString(Constants.ENCODING_UTF8);
                        StructuralDirective structuralDirective = gson.fromJson(directiveString, new TypeToken<StructuralDirective>() {
                        }.getType());
                        structuralDirective.setDirectiveParent(directiveString);
                        arrayList.add(structuralDirective);
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "parse: directiveString:\n" + directiveString);
                        }
                    } else if (headers.contains(UtilsNetwork.OCTET_STREAM)) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "parse: audio");
                        }
                        multiPartList.add(new Pair<>(getMultipartContentId(headers), byteArrayOutputStream.toByteArray()));
                    } else if (DEBUG) {
                        MyLog.w(CLS_NAME, "parse: unknown: " + headers);
                    }
                    isFirst = false;
                }
                directiveList.setMultiPartList(multiPartList);
            } else {
                if (DEBUG) {
                    MyLog.i(CLS_NAME, "parse: skipPreamble: false Response Body:\n" + responseString);
                }
                StructuralDirective structuralDirective = gson.fromJson(responseString, new com.google.common.reflect.TypeToken<StructuralDirective>() {
                }.getType());
                structuralDirective.setDirectiveParent(responseString);
                arrayList.add(structuralDirective);
            }
            if (DEBUG) {
                MyLog.i(CLS_NAME, "parse");
                MyLog.i(CLS_NAME, "parse: directives size: " + arrayList.size());
                MyLog.i(CLS_NAME, "parse: audio size: " + multiPartList.size());
                MyLog.getElapsed(CLS_NAME, "parse", nanoTime);
            }
            if (responseString.contains("Exception")) {
                if (DEBUG) {
                    MyLog.w(CLS_NAME, "parse: header: isException: true");
                }
                directiveList.setErrorCode(DirectiveList.RESPONSE_EXCEPTION);
                return directiveList;
            }

            int directiveAction = DirectiveList.ACTION_DEFAULT;
            boolean hasDirectiveCancel = false;
            if (UtilsList.notNaked(arrayList)) {
                for (StructuralDirective next : arrayList) {
                    String jSONObject = new JSONObject(next.getDirectiveParent()).toString(4);
                    Directive directive = next.getDirective();
                    Payload payload = directive.getPayload();
                    Header header = directive.getHeader();
                    if (DEBUG) {
                        MyLog.i(CLS_NAME, "parse: directiveParent json: " + jSONObject);
                        MyLog.i(CLS_NAME, "parse: directive: " + directive);
                        MyLog.i(CLS_NAME, "parse: header: " + header.toString());
                        MyLog.i(CLS_NAME, "parse: payload: " + payload.toString());
                        MyLog.i(CLS_NAME, "parse: payload: isCancel: " + payload.isCancel());
                        MyLog.i(CLS_NAME, "parse: payload: isAbandon: " + payload.isAbandon());
                    }
                    if (payload.isCancel()) {
                        next.setDirectiveType(DirectiveType.DIRECTIVE_CANCEL);
                        hasDirectiveCancel = true;
                    } else if (payload.isAbandon()) {
                        next.setDirectiveType(DirectiveType.DIRECTIVE_ABANDON);
                    } else if (next.getDirective().isDirectiveMedia()) {
                        next.setDirectiveType(DirectiveType.DIRECTIVE_MEDIA);
                    } else if (next.getDirective().isDirectiveVolume()) {
                        next.setDirectiveType(DirectiveType.DIRECTIVE_VOLUME);
                    } else if (next.getDirective().isExpectSpeech()) {
                        if (DEBUG) {
                            MyLog.i(CLS_NAME, "parse: directive: isExpectSpeech: true");
                        }
                        directiveAction = DirectiveList.ACTION_EXPECT_SPEECH;
                    }
                }
            }
            directiveList.setDirectiveList(arrayList);
            if (hasDirectiveCancel || !UtilsList.notNaked(multiPartList)) {
                if (!UtilsList.notNaked(arrayList)) {
                    if (DEBUG) {
                        MyLog.w(CLS_NAME, "parse: no audio or directives");
                    }
                    directiveList.setErrorCode(DirectiveList.NO_AUDIO_OR_DIRECTIVES);
                } else if (DEBUG) {
                    MyLog.i(CLS_NAME, "parse: no audio but have directives");
                }
            } else if (file != null) {
                try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file))) {
                    bufferedOutputStream.write(multiPartList.get(0).second);
                    bufferedOutputStream.flush();
                }
                directiveList.setFile(file);
                directiveList.setAction(directiveAction);
            } else {
                directiveList.setAction(directiveAction);
            }
            return directiveList;
        } catch (FileNotFoundException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "parse: FileNotFoundException: missing file");
                e.printStackTrace();
            }
        } catch (IOException e) {
            if (DEBUG) {
                MyLog.e(CLS_NAME, "parse: IOException: missing bytes");
                e.printStackTrace();
            }
        }
        directiveList.setErrorCode(DirectiveList.MISSING_BYTES);
        return directiveList;
    }

    private String getString(byte[] bytes) {
        try {
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return new String(bytes, Charset.defaultCharset());
        }
    }
}
