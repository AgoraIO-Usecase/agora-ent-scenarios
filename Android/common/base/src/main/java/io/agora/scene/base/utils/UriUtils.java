package io.agora.scene.base.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import kotlin.collections.CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Regex;
import kotlin.text.StringsKt;

/**
 * 代码内kotlin转回java
 */
public final class UriUtils {
    @NotNull
    public static final UriUtils INSTANCE;

    @Nullable
    public final String getFilePathByUri(@NotNull Context context, @NotNull Uri uri) {
        Intrinsics.checkNotNullParameter(context, "context");
        Intrinsics.checkNotNullParameter(uri, "uri");
        String path = (String) null;
        if (Intrinsics.areEqual("file", uri.getScheme())) {
            path = uri.getPath();
            return path;
        } else {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                String docId;
                String[] split;
                CharSequence var6;
                String var7;
                boolean var8;
                boolean var9;
                boolean var10;
                CharSequence var11;
                boolean var12;
                List $this$dropLastWhile$iv;
                List var10000;
                Collection $this$toTypedArray$iv;
                String type;
                Regex var19;
                boolean $i$f$toTypedArray;
                byte var21;
                ListIterator iterator$iv;
                String it;
                Object[] var27;
                if (this.isExternalStorageDocument(uri)) {
                    label99:
                    {
                        docId = DocumentsContract.getDocumentId(uri);
                        Intrinsics.checkNotNullExpressionValue(docId, "docId");
                        var6 = (CharSequence) docId;
                        var7 = ":";
                        var8 = false;
                        var19 = new Regex(var7);
                        var21 = 0;
                        var9 = false;
                        $this$dropLastWhile$iv = var19.split(var6, var21);
                        $i$f$toTypedArray = false;
                        if (!$this$dropLastWhile$iv.isEmpty()) {
                            iterator$iv = $this$dropLastWhile$iv.listIterator($this$dropLastWhile$iv.size());

                            while (iterator$iv.hasPrevious()) {
                                it = (String) iterator$iv.previous();
                                var10 = false;
                                var11 = (CharSequence) it;
                                var12 = false;
                                if (var11.length() != 0) {
                                    var10000 = CollectionsKt.take((Iterable) $this$dropLastWhile$iv, iterator$iv.nextIndex() + 1);
                                    break label99;
                                }
                            }
                        }

                        var10000 = CollectionsKt.emptyList();
                    }

                    $this$toTypedArray$iv = (Collection) var10000;
                    $i$f$toTypedArray = false;
                    var27 = $this$toTypedArray$iv.toArray(new String[0]);
                    if (var27 == null) {
                        throw new NullPointerException("null cannot be cast to non-null type kotlin.Array<T>");
                    }

                    split = (String[]) var27;
                    type = split[0];
                    if (StringsKt.equals("primary", type, true)) {
                        path = Environment.getExternalStorageDirectory().toString() + "/" + split[1];
                        return path;
                    }
                } else {
                    if (this.isDownloadsDocument(uri)) {
                        docId = DocumentsContract.getDocumentId(uri);
                        Uri var28 = Uri.parse("content://downloads/public_downloads");
                        Long var10001 = Long.valueOf(docId);
                        Intrinsics.checkNotNullExpressionValue(var10001, "java.lang.Long.valueOf(id)");
                        var28 = ContentUris.withAppendedId(var28, var10001);
                        Intrinsics.checkNotNullExpressionValue(var28, "ContentUris.withAppended…eOf(id)\n                )");
                        Uri contentUri = var28;
                        path = this.getDataColumn(context, contentUri, (String) null, (String[]) null);
                        return path;
                    }

                    if (this.isMediaDocument(uri)) {
                        label86:
                        {
                            docId = DocumentsContract.getDocumentId(uri);
                            Intrinsics.checkNotNullExpressionValue(docId, "docId");
                            var6 = (CharSequence) docId;
                            var7 = ":";
                            var8 = false;
                            var19 = new Regex(var7);
                            var21 = 0;
                            var9 = false;
                            $this$dropLastWhile$iv = var19.split(var6, var21);
                            $i$f$toTypedArray = false;
                            if (!$this$dropLastWhile$iv.isEmpty()) {
                                iterator$iv = $this$dropLastWhile$iv.listIterator($this$dropLastWhile$iv.size());

                                while (iterator$iv.hasPrevious()) {
                                    it = (String) iterator$iv.previous();
                                    var10 = false;
                                    var11 = (CharSequence) it;
                                    var12 = false;
                                    if (var11.length() != 0) {
                                        var10000 = CollectionsKt.take((Iterable) $this$dropLastWhile$iv, iterator$iv.nextIndex() + 1);
                                        break label86;
                                    }
                                }
                            }

                            var10000 = CollectionsKt.emptyList();
                        }

                        $this$toTypedArray$iv = (Collection) var10000;
                        $i$f$toTypedArray = false;
                        var27 = $this$toTypedArray$iv.toArray(new String[0]);
                        if (var27 == null) {
                            throw new NullPointerException("null cannot be cast to non-null type kotlin.Array<T>");
                        }

                        split = (String[]) var27;
                        type = split[0];
                        Uri contentUri = (Uri) null;
                        switch (type.hashCode()) {
                            case 93166550:
                                if (type.equals("audio")) {
                                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                                }
                                break;
                            case 100313435:
                                if (type.equals("image")) {
                                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                                }
                                break;
                            case 112202875:
                                if (type.equals("video")) {
                                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                                }
                        }

                        String selection = "_id=?";
                        String[] selectionArgs = new String[]{split[1]};
                        path = this.getDataColumn(context, contentUri, selection, selectionArgs);
                        return path;
                    }
                }
            } else if (Intrinsics.areEqual("content", uri.getScheme())) {
                Cursor cursor = context.getContentResolver().query(uri, new String[]{"_data"}, (String) null, (String[]) null, (String) null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndexOrThrow("_data");
                        if (columnIndex > -1) {
                            path = cursor.getString(columnIndex);
                        }
                    }

                    cursor.close();
                }

                return path;
            }

            return uri.toString();
        }
    }

    private final String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = (Cursor) null;
        String column = "_data";
        String[] projection = new String[]{column};
        boolean var11 = false;

        String var9;
        label73:
        {
            try {
                var11 = true;
                ContentResolver var10000 = context.getContentResolver();
                Intrinsics.checkNotNull(uri);
                cursor = var10000.query(uri, projection, selection, selectionArgs, (String) null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int column_index = cursor.getColumnIndexOrThrow(column);
                        var9 = cursor.getString(column_index);
                        var11 = false;
                        break label73;
                    }

                    var11 = false;
                } else {
                    var11 = false;
                }
            } finally {
                if (var11) {
                    if (cursor != null) {
                        cursor.close();
                    }

                }
            }

            if (cursor != null) {
                cursor.close();
            }

            return null;
        }

        cursor.close();
        return var9;
    }

    private final boolean isExternalStorageDocument(Uri uri) {
        return Intrinsics.areEqual("com.android.externalstorage.documents", uri.getAuthority());
    }

    private final boolean isDownloadsDocument(Uri uri) {
        return Intrinsics.areEqual("com.android.providers.downloads.documents", uri.getAuthority());
    }

    private final boolean isMediaDocument(Uri uri) {
        return Intrinsics.areEqual("com.android.providers.media.documents", uri.getAuthority());
    }

    private UriUtils() {
    }

    static {
        UriUtils var0 = new UriUtils();
        INSTANCE = var0;
    }
}
