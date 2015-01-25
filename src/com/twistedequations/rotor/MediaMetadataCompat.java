/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.twistedequations.rotor;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataEditor;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import java.util.Set;

/**
 * Contains metadata about an item, such as the title, artist, etc.
 */
public final class MediaMetadataCompat implements Parcelable {
    private static final String TAG = "MediaMetadata";

    /**
     * The title of the media.
     */
    public static final String METADATA_KEY_TITLE = "android.media.metadata.TITLE";

    /**
     * The artist of the media.
     */
    public static final String METADATA_KEY_ARTIST = "android.media.metadata.ARTIST";

    /**
     * The duration of the media in ms. A negative duration indicates that the
     * duration is unknown (or infinite).
     */
    public static final String METADATA_KEY_DURATION = "android.media.metadata.DURATION";

    /**
     * The album title for the media.
     */
    public static final String METADATA_KEY_ALBUM = "android.media.metadata.ALBUM";

    /**
     * The author of the media.
     */
    public static final String METADATA_KEY_AUTHOR = "android.media.metadata.AUTHOR";

    /**
     * The writer of the media.
     */
    public static final String METADATA_KEY_WRITER = "android.media.metadata.WRITER";

    /**
     * The composer of the media.
     */
    public static final String METADATA_KEY_COMPOSER = "android.media.metadata.COMPOSER";

    /**
     * The compilation status of the media.
     */
    public static final String METADATA_KEY_COMPILATION = "android.media.metadata.COMPILATION";

    /**
     * The date the media was created or published. The format is unspecified
     * but RFC 3339 is recommended.
     */
    public static final String METADATA_KEY_DATE = "android.media.metadata.DATE";

    /**
     * The year the media was created or published as a long.
     */
    public static final String METADATA_KEY_YEAR = "android.media.metadata.YEAR";

    /**
     * The genre of the media.
     */
    public static final String METADATA_KEY_GENRE = "android.media.metadata.GENRE";

    /**
     * The track number for the media.
     */
    public static final String METADATA_KEY_TRACK_NUMBER = "android.media.metadata.TRACK_NUMBER";

    /**
     * The number of tracks in the media's original source.
     */
    public static final String METADATA_KEY_NUM_TRACKS = "android.media.metadata.NUM_TRACKS";

    /**
     * The disc number for the media's original source.
     */
    public static final String METADATA_KEY_DISC_NUMBER = "android.media.metadata.DISC_NUMBER";

    /**
     * The artist for the album of the media's original source.
     */
    public static final String METADATA_KEY_ALBUM_ARTIST = "android.media.metadata.ALBUM_ARTIST";

    /**
     * The artwork for the media as a {@link Bitmap}.
     */
    public static final String METADATA_KEY_ART = "android.media.metadata.ART";

    /**
     * The artwork for the media as a Uri formatted String. The artwork can be
     * loaded using a combination of {@link ContentResolver#openInputStream} and
     * {@link BitmapFactory#decodeStream}.
     */
    public static final String METADATA_KEY_ART_URI = "android.media.metadata.ART_URI";

    /**
     * The artwork for the album of the media's original source as a
     * {@link Bitmap}.
     */
    public static final String METADATA_KEY_ALBUM_ART = "android.media.metadata.ALBUM_ART";

    /**
     * The artwork for the album of the media's original source as a Uri
     * formatted String. The artwork can be loaded using a combination of
     * {@link ContentResolver#openInputStream} and
     * {@link BitmapFactory#decodeStream}.
     */
    public static final String METADATA_KEY_ALBUM_ART_URI = "android.media.metadata.ALBUM_ART_URI";

    /**
     * The user's rating for the media.
     *
     * @see RatingCompat
     */
    public static final String METADATA_KEY_USER_RATING = "android.media.metadata.USER_RATING";

    /**
     * The overall rating for the media.
     *
     * @see RatingCompat
     */
    public static final String METADATA_KEY_RATING = "android.media.metadata.RATING";

    /**
     * A title that is suitable for display to the user. This will generally be
     * the same as {@link #METADATA_KEY_TITLE} but may differ for some formats.
     * When displaying media described by this metadata this should be preferred
     * if present.
     */
    public static final String METADATA_KEY_DISPLAY_TITLE = "android.media.metadata.DISPLAY_TITLE";

    /**
     * A subtitle that is suitable for display to the user. When displaying a
     * second line for media described by this metadata this should be preferred
     * to other fields if present.
     */
    public static final String METADATA_KEY_DISPLAY_SUBTITLE
            = "android.media.metadata.DISPLAY_SUBTITLE";

    /**
     * A description that is suitable for display to the user. When displaying
     * more information for media described by this metadata this should be
     * preferred to other fields if present.
     */
    public static final String METADATA_KEY_DISPLAY_DESCRIPTION
            = "android.media.metadata.DISPLAY_DESCRIPTION";

    /**
     * An icon or thumbnail that is suitable for display to the user. When
     * displaying an icon for media described by this metadata this should be
     * preferred to other fields if present. This must be a {@link Bitmap}.
     */
    public static final String METADATA_KEY_DISPLAY_ICON
            = "android.media.metadata.DISPLAY_ICON";

    /**
     * A Uri formatted String for an icon or thumbnail that is suitable for
     * display to the user. When displaying more information for media described
     * by this metadata the display description should be preferred to other
     * fields when present. The icon can be loaded using a combination of
     * {@link ContentResolver#openInputStream} and
     * {@link BitmapFactory#decodeStream}.
     */
    public static final String METADATA_KEY_DISPLAY_ICON_URI
            = "android.media.metadata.DISPLAY_ICON_URI";

    /**
     * A String key for identifying the content. This value is specific to the
     * service providing the content. If used, this should be a persistent
     * unique key for the underlying content. It may be used with
     * {@link MediaController.TransportControls#playFromMediaId(String, Bundle)}
     * to initiate playback when provided by a {@link MediaBrowser} connected to
     * the same app.
     */
    public static final String METADATA_KEY_MEDIA_ID = "android.media.metadata.MEDIA_ID";

    private static final String[] PREFERRED_DESCRIPTION_ORDER = {
            METADATA_KEY_TITLE,
            METADATA_KEY_ARTIST,
            METADATA_KEY_ALBUM,
            METADATA_KEY_ALBUM_ARTIST,
            METADATA_KEY_WRITER,
            METADATA_KEY_AUTHOR,
            METADATA_KEY_COMPOSER
    };

    private static final String[] PREFERRED_BITMAP_ORDER = {
            METADATA_KEY_DISPLAY_ICON,
            METADATA_KEY_ART,
            METADATA_KEY_ALBUM_ART
    };

    private static final String[] PREFERRED_URI_ORDER = {
            METADATA_KEY_DISPLAY_ICON_URI,
            METADATA_KEY_ART_URI,
            METADATA_KEY_ALBUM_ART_URI
    };

    private static final int METADATA_TYPE_INVALID = -1;
    private static final int METADATA_TYPE_LONG = 0;
    private static final int METADATA_TYPE_TEXT = 1;
    private static final int METADATA_TYPE_BITMAP = 2;
    private static final int METADATA_TYPE_RATING = 3;
    private static final ArrayMap<String, Integer> METADATA_KEYS_TYPE;
    private static SparseArray<Integer> EDITOR_KEYS_TYPE;

    static {
        METADATA_KEYS_TYPE = new ArrayMap<String, Integer>(26);
        METADATA_KEYS_TYPE.put(METADATA_KEY_TITLE, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ARTIST, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DURATION, METADATA_TYPE_LONG);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ALBUM, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_AUTHOR, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_WRITER, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_COMPOSER, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_COMPILATION, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DATE, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_YEAR, METADATA_TYPE_LONG);
        METADATA_KEYS_TYPE.put(METADATA_KEY_GENRE, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_TRACK_NUMBER, METADATA_TYPE_LONG);
        METADATA_KEYS_TYPE.put(METADATA_KEY_NUM_TRACKS, METADATA_TYPE_LONG);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISC_NUMBER, METADATA_TYPE_LONG);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ALBUM_ARTIST, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ART, METADATA_TYPE_BITMAP);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ART_URI, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ALBUM_ART, METADATA_TYPE_BITMAP);
        METADATA_KEYS_TYPE.put(METADATA_KEY_ALBUM_ART_URI, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_USER_RATING, METADATA_TYPE_RATING);
        METADATA_KEYS_TYPE.put(METADATA_KEY_RATING, METADATA_TYPE_RATING);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_TITLE, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_SUBTITLE, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_DESCRIPTION, METADATA_TYPE_TEXT);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_ICON, METADATA_TYPE_BITMAP);
        METADATA_KEYS_TYPE.put(METADATA_KEY_DISPLAY_ICON_URI, METADATA_TYPE_TEXT);
    }

    //This is populated later in a lazy fashion
    private static SparseArray<String> EDITOR_KEY_MAPPING;

    private final Bundle mBundle;
    private MediaDescriptionCompat mDescription;

    private MediaMetadataCompat(Bundle bundle) {
        mBundle = new Bundle(bundle);
    }

    private MediaMetadataCompat(Parcel in) {
        mBundle = in.readBundle();
    }

    /**
     * Returns true if the given key is contained in the metadata
     *
     * @param key a String key
     * @return true if the key exists in this metadata, false otherwise
     */
    public boolean containsKey(String key) {
        return mBundle.containsKey(key);
    }

    /**
     * Returns the value associated with the given key, or null if no mapping of
     * the desired type exists for the given key or a null value is explicitly
     * associated with the key.
     *
     * @param key The key the value is stored under
     * @return a CharSequence value, or null
     */
    public CharSequence getText(String key) {
        return mBundle.getCharSequence(key);
    }

    /**
     * Returns the text value associated with the given key as a String, or null
     * if no mapping of the desired type exists for the given key or a null
     * value is explicitly associated with the key. This is equivalent to
     * calling {@link #getText getText().toString()} if the value is not null.
     *
     * @param key The key the value is stored under
     * @return a String value, or null
     */
    public String getString(String key) {
        CharSequence text = getText(key);
        if (text != null) {
            return text.toString();
        }
        return null;
    }

    /**
     * Returns the value associated with the given key, or 0L if no long exists
     * for the given key.
     *
     * @param key The key the value is stored under
     * @return a long value
     */
    public long getLong(String key) {
        return mBundle.getLong(key, 0);
    }

    /**
     * Returns a {@link RatingCompat} for the given key or null if no rating exists
     * for the given key.
     *
     * @param key The key the value is stored under
     * @return A {@link RatingCompat} or null
     */
    public RatingCompat getRating(String key) {
        RatingCompat ratingCompat = null;
        try {
            ratingCompat = mBundle.getParcelable(key);
        } catch (Exception e) {
            // ignore, value was not a bitmap
            Log.w(TAG, "Failed to retrieve a key as Rating.", e);
        }
        return ratingCompat;
    }

    /**
     * Returns a {@link Bitmap} for the given key or null if no bitmap exists
     * for the given key.
     *
     * @param key The key the value is stored under
     * @return A {@link Bitmap} or null
     */
    public Bitmap getBitmap(String key) {
        Bitmap bmp = null;
        try {
            bmp = mBundle.getParcelable(key);
        } catch (Exception e) {
            // ignore, value was not a bitmap
            Log.w(TAG, "Failed to retrieve a key as Bitmap.", e);
        }
        return bmp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBundle(mBundle);
    }

    /**
     * Returns the number of fields in this metadata.
     *
     * @return The number of fields in the metadata.
     */
    public int size() {
        return mBundle.size();
    }

    /**
     * Returns a Set containing the Strings used as keys in this metadata.
     *
     * @return a Set of String keys
     */
    public Set<String> keySet() {
        return mBundle.keySet();
    }

    /**
     * Returns a simple description of this metadata for display purposes. Used for pre KITKAT devices
     * @return A simple description of this metadata.
     */
    public MediaDescriptionCompat getDescription() {
        if (mDescription != null) {
            return mDescription;
        }

        String mediaId = getString(METADATA_KEY_MEDIA_ID);

        CharSequence[] text = new CharSequence[3];
        Bitmap icon = null;
        Uri iconUri = null;

        // First handle the case where display data is set already
        CharSequence displayText = getText(METADATA_KEY_DISPLAY_TITLE);
        if (!TextUtils.isEmpty(displayText)) {
            // If they have a display title use only display data, otherwise use
            // our best bets
            text[0] = displayText;
            text[1] = getText(METADATA_KEY_DISPLAY_SUBTITLE);
            text[2] = getText(METADATA_KEY_DISPLAY_DESCRIPTION);
        } else {
            // Use whatever fields we can
            int textIndex = 0;
            int keyIndex = 0;
            while (textIndex < text.length && keyIndex < PREFERRED_DESCRIPTION_ORDER.length) {
                CharSequence next = getText(PREFERRED_DESCRIPTION_ORDER[keyIndex++]);
                if (!TextUtils.isEmpty(next)) {
                    // Fill in the next empty bit of text
                    text[textIndex++] = next;
                }
            }
        }

        // Get the best art bitmap we can find
        for (int i = 0; i < PREFERRED_BITMAP_ORDER.length; i++) {
            Bitmap next = getBitmap(PREFERRED_BITMAP_ORDER[i]);
            if (next != null) {
                icon = next;
                break;
            }
        }

        // Get the best Uri we can find
        for (int i = 0; i < PREFERRED_URI_ORDER.length; i++) {
            String next = getString(PREFERRED_URI_ORDER[i]);
            if (!TextUtils.isEmpty(next)) {
                iconUri = Uri.parse(next);
                break;
            }
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            MediaDescriptionCompat mediaDescriptionCompat = new MediaDescriptionCompat();
            mediaDescriptionCompat.mediaID = mediaId;
            mediaDescriptionCompat.title = text[0];
            mediaDescriptionCompat.subtitle = text[1];
            mediaDescriptionCompat.description = text[2];
            mediaDescriptionCompat.icon = icon;
            mediaDescriptionCompat.iconUri = iconUri;
            mDescription = mediaDescriptionCompat;
        }


        return mDescription;
    }

    /**
     * Maps the values in the metadata to the equivalent fields in the {@link android.media.RemoteControlClient}
     * @param remoteControlClient the RemoteControlClient to populate
     * @return the populated RemoteControlClient
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public RemoteControlClient populateRemoteControlClient(RemoteControlClient remoteControlClient) {

        if(EDITOR_KEY_MAPPING == null) {
            //Lazy fill the mapping
            fillEditorKeyMapping();
        }

        if(EDITOR_KEYS_TYPE == null) {
            //Lazy fill the mapping
            fillEditorTypeMapping();
        }

        String compatKey;
        int editorKey;
        RemoteControlClient.MetadataEditor editor = remoteControlClient.editMetadata(true);
        for (int i = 0; i < EDITOR_KEY_MAPPING.size(); i++) {
            editorKey = EDITOR_KEY_MAPPING.keyAt(i);
            compatKey = EDITOR_KEY_MAPPING.get(editorKey);
            int type = METADATA_KEYS_TYPE.get(compatKey);
            Integer editorType = EDITOR_KEYS_TYPE.get(editorKey);

            switch(type) {

                case METADATA_TYPE_TEXT:
                    String data = getString(compatKey);
                    if(data != null && editorType != null && editorType == METADATA_TYPE_TEXT) {
                        editor.putString(editorKey, data);
                    }
                    break;

                case METADATA_TYPE_BITMAP:
                    Bitmap bitmap = getBitmap(compatKey);
                    if(bitmap != null && editorType != null && editorType == METADATA_TYPE_BITMAP) {
                        editor.putBitmap(editorKey, bitmap);
                    }
                    break;

                case METADATA_TYPE_LONG:
                    long valueLong = getLong(compatKey);
                    if(editorType != null && editorType == METADATA_TYPE_LONG) {
                        editor.putLong(editorKey, valueLong);
                    }
                    break;

                case METADATA_TYPE_RATING:
                    RatingCompat ratingCompat = getRating(compatKey);
                    if(editorType != null && editorType == METADATA_TYPE_RATING) {
                        editor.putObject(editorKey, ratingCompat.getRating());
                    }
                    break;
            }
        }
        editor.apply();
        return remoteControlClient;
    }

    /**
     * Maps the metadata to the android system MediaMetadata object for use in media sessions
     * @return a populated MediaMetadata object.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public android.media.MediaMetadata getMediaSessionData() {

        final android.media.MediaMetadata.Builder builder = new android.media.MediaMetadata.Builder();
        Set<String> keys = METADATA_KEYS_TYPE.keySet();

        for (String key : keys) {
            int type = METADATA_KEYS_TYPE.get(key);

            switch(type) {

                case METADATA_TYPE_TEXT:
                    String data = getString(key);
                    if(data != null) {
                        builder.putString(key, data);
                    }
                    break;

                case METADATA_TYPE_BITMAP:
                    Bitmap bitmap = getBitmap(key);
                    if(bitmap != null) {
                        builder.putBitmap(key, bitmap);
                    }
                    break;

                case METADATA_TYPE_LONG:
                    long valueLong = getLong(key);
                    builder.putLong(key, valueLong);
                    break;

                case METADATA_TYPE_RATING:
                    RatingCompat ratingCompat = getRating(key);
                    builder.putRating(key, ratingCompat.getRating());
                    break;
            }
        }
        return builder.build();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void fillEditorKeyMapping() {
        EDITOR_KEY_MAPPING = new SparseArray<String>();
        EDITOR_KEY_MAPPING.put(MediaMetadataEditor.BITMAP_KEY_ARTWORK, METADATA_KEY_ART);
        EDITOR_KEY_MAPPING.put(MediaMetadataEditor.RATING_KEY_BY_OTHERS, METADATA_KEY_RATING);
        EDITOR_KEY_MAPPING.put(MediaMetadataEditor.RATING_KEY_BY_USER, METADATA_KEY_USER_RATING);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_ALBUM, METADATA_KEY_ALBUM);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, METADATA_KEY_ALBUM_ARTIST);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_ARTIST, METADATA_KEY_ARTIST);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_AUTHOR, METADATA_KEY_AUTHOR);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER, METADATA_KEY_TRACK_NUMBER);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_COMPOSER, METADATA_KEY_COMPOSER);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_COMPILATION, METADATA_KEY_COMPILATION);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_DATE, METADATA_KEY_DATE);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER, METADATA_KEY_DISC_NUMBER);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_DURATION, METADATA_KEY_DURATION);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_GENRE, METADATA_KEY_GENRE);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_NUM_TRACKS, METADATA_KEY_NUM_TRACKS);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_TITLE, METADATA_KEY_TITLE);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_WRITER, METADATA_KEY_WRITER);
        EDITOR_KEY_MAPPING.put(MediaMetadataRetriever.METADATA_KEY_YEAR, METADATA_KEY_YEAR);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void fillEditorTypeMapping() {
        EDITOR_KEYS_TYPE = new SparseArray<Integer>(26);
        EDITOR_KEYS_TYPE.put(MediaMetadataEditor.BITMAP_KEY_ARTWORK, METADATA_TYPE_BITMAP);
        EDITOR_KEYS_TYPE.put(MediaMetadataEditor.RATING_KEY_BY_OTHERS, METADATA_TYPE_RATING);
        EDITOR_KEYS_TYPE.put(MediaMetadataEditor.RATING_KEY_BY_USER, METADATA_TYPE_RATING);
        EDITOR_KEYS_TYPE.put(MediaMetadataRetriever.METADATA_KEY_ALBUM, METADATA_TYPE_TEXT);
        EDITOR_KEYS_TYPE.put(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST, METADATA_TYPE_TEXT);
        EDITOR_KEYS_TYPE.put(MediaMetadataRetriever.METADATA_KEY_ARTIST, METADATA_TYPE_TEXT);
        EDITOR_KEYS_TYPE.put(MediaMetadataRetriever.METADATA_KEY_AUTHOR, METADATA_TYPE_TEXT);
        EDITOR_KEYS_TYPE.put(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER, METADATA_TYPE_LONG);
        EDITOR_KEYS_TYPE.put(MediaMetadataRetriever.METADATA_KEY_COMPOSER, METADATA_TYPE_TEXT);
        EDITOR_KEYS_TYPE.put(MediaMetadataRetriever.METADATA_KEY_COMPILATION, METADATA_TYPE_TEXT);
        EDITOR_KEYS_TYPE.put(MediaMetadataRetriever.METADATA_KEY_DATE, METADATA_TYPE_TEXT);
        EDITOR_KEYS_TYPE.put(MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER, METADATA_TYPE_LONG);
        EDITOR_KEYS_TYPE.put(MediaMetadataRetriever.METADATA_KEY_DURATION, METADATA_TYPE_LONG);
        EDITOR_KEYS_TYPE.put(MediaMetadataRetriever.METADATA_KEY_YEAR, METADATA_TYPE_LONG);
        EDITOR_KEYS_TYPE.put(MediaMetadataRetriever.METADATA_KEY_GENRE, METADATA_TYPE_TEXT);
        EDITOR_KEYS_TYPE.put(MediaMetadataRetriever.METADATA_KEY_TITLE, METADATA_TYPE_TEXT);
        EDITOR_KEYS_TYPE.put(MediaMetadataRetriever.METADATA_KEY_WRITER, METADATA_TYPE_TEXT);
    }

    @Override
    public boolean equals(Object o) {
        if(o != null && o instanceof MediaMetadataCompat) {
            MediaMetadataCompat other = (MediaMetadataCompat) o;
            //compare the bundles
            if(other.mBundle == null || mBundle == null) {
                return false;
            }

            //compare size, not equal if sizes are different
            if(other.mBundle.size() != mBundle.size()) {
                return false;
            }

            Set<String> keys = mBundle.keySet();
            for(String key : keys) {
                if(!other.mBundle.containsKey(key)) {
                    //key is missing, not equal
                    return false;
                }

                Object value = mBundle.get(key);
                Object valueOther = other.mBundle.get(key);
                if(!value.equals(valueOther)) {
                    return false;
                }
            }

            return true;
        }
        else {
            return false;
        }
    }

    public static final Parcelable.Creator<MediaMetadataCompat> CREATOR =
            new Parcelable.Creator<MediaMetadataCompat>() {
                @Override
                public MediaMetadataCompat createFromParcel(Parcel in) {
                    return new MediaMetadataCompat(in);
                }

                @Override
                public MediaMetadataCompat[] newArray(int size) {
                    return new MediaMetadataCompat[size];
                }
            };

    /**
     * Use to build MediaMetadata objects. The system defined metadata keys must
     * use the appropriate data type.
     */
    public static final class Builder {
        private final Bundle mBundle;

        /**
         * Create an empty Builder. Any field that should be included in the
         * {@link MediaMetadataCompat} must be added.
         */
        public Builder() {
            mBundle = new Bundle();
        }

        /**
         * Create a Builder using a {@link MediaMetadataCompat} instance to set the
         * initial values. All fields in the source metadata will be included in
         * the new metadata. Fields can be overwritten by adding the same key.
         *
         * @param source
         */
        public Builder(MediaMetadataCompat source) {
            mBundle = new Bundle(source.mBundle);
        }

        /**
         * Put a CharSequence value into the metadata. Custom keys may be used,
         * but if the METADATA_KEYs defined in this class are used they may only
         * be one of the following:
         * <ul>
         * <li>{@link #METADATA_KEY_TITLE}</li>
         * <li>{@link #METADATA_KEY_ARTIST}</li>
         * <li>{@link #METADATA_KEY_ALBUM}</li>
         * <li>{@link #METADATA_KEY_AUTHOR}</li>
         * <li>{@link #METADATA_KEY_WRITER}</li>
         * <li>{@link #METADATA_KEY_COMPOSER}</li>
         * <li>{@link #METADATA_KEY_DATE}</li>
         * <li>{@link #METADATA_KEY_GENRE}</li>
         * <li>{@link #METADATA_KEY_ALBUM_ARTIST}</li>
         * <li>{@link #METADATA_KEY_ART_URI}</li>
         * <li>{@link #METADATA_KEY_ALBUM_ART_URI}</li>
         * <li>{@link #METADATA_KEY_DISPLAY_TITLE}</li>
         * <li>{@link #METADATA_KEY_DISPLAY_SUBTITLE}</li>
         * <li>{@link #METADATA_KEY_DISPLAY_DESCRIPTION}</li>
         * <li>{@link #METADATA_KEY_DISPLAY_ICON_URI}</li>
         * </ul>
         *
         * @param key The key for referencing this value
         * @param value The CharSequence value to store
         * @return The Builder to allow chaining
         */
        public Builder putText(String key, CharSequence value) {
            if (METADATA_KEYS_TYPE.containsKey(key)) {
                if (METADATA_KEYS_TYPE.get(key) != METADATA_TYPE_TEXT) {
                    throw new IllegalArgumentException("The " + key
                            + " key cannot be used to put a CharSequence");
                }
            }
            mBundle.putCharSequence(key, value);
            return this;
        }

        /**
         * Put a String value into the metadata. Custom keys may be used, but if
         * the METADATA_KEYs defined in this class are used they may only be one
         * of the following:
         * <ul>
         * <li>{@link #METADATA_KEY_TITLE}</li>
         * <li>{@link #METADATA_KEY_ARTIST}</li>
         * <li>{@link #METADATA_KEY_ALBUM}</li>
         * <li>{@link #METADATA_KEY_AUTHOR}</li>
         * <li>{@link #METADATA_KEY_WRITER}</li>
         * <li>{@link #METADATA_KEY_COMPOSER}</li>
         * <li>{@link #METADATA_KEY_DATE}</li>
         * <li>{@link #METADATA_KEY_GENRE}</li>
         * <li>{@link #METADATA_KEY_ALBUM_ARTIST}</li>
         * <li>{@link #METADATA_KEY_ART_URI}</li>
         * <li>{@link #METADATA_KEY_ALBUM_ART_URI}</li>
         * <li>{@link #METADATA_KEY_DISPLAY_TITLE}</li>
         * <li>{@link #METADATA_KEY_DISPLAY_SUBTITLE}</li>
         * <li>{@link #METADATA_KEY_DISPLAY_DESCRIPTION}</li>
         * <li>{@link #METADATA_KEY_DISPLAY_ICON_URI}</li>
         * </ul>
         *
         * @param key The key for referencing this value
         * @param value The String value to store
         * @return The Builder to allow chaining
         */
        public Builder putString(String key, String value) {
            if (METADATA_KEYS_TYPE.containsKey(key)) {
                if (METADATA_KEYS_TYPE.get(key) != METADATA_TYPE_TEXT) {
                    throw new IllegalArgumentException("The " + key
                            + " key cannot be used to put a String");
                }
            }
            mBundle.putCharSequence(key, value);
            return this;
        }

        /**
         * Put a long value into the metadata. Custom keys may be used, but if
         * the METADATA_KEYs defined in this class are used they may only be one
         * of the following:
         * <ul>
         * <li>{@link #METADATA_KEY_DURATION}</li>
         * <li>{@link #METADATA_KEY_TRACK_NUMBER}</li>
         * <li>{@link #METADATA_KEY_NUM_TRACKS}</li>
         * <li>{@link #METADATA_KEY_DISC_NUMBER}</li>
         * <li>{@link #METADATA_KEY_YEAR}</li>
         * </ul>
         *
         * @param key The key for referencing this value
         * @param value The long value to store
         * @return The Builder to allow chaining
         */
        public Builder putLong(String key, long value) {
            if (METADATA_KEYS_TYPE.containsKey(key)) {
                if (METADATA_KEYS_TYPE.get(key) != METADATA_TYPE_LONG) {
                    throw new IllegalArgumentException("The " + key
                            + " key cannot be used to put a long");
                }
            }
            mBundle.putLong(key, value);
            return this;
        }

        /**
         * Put a {@link RatingCompat} into the metadata. Custom keys may be used, but
         * if the METADATA_KEYs defined in this class are used they may only be
         * one of the following:
         * <ul>
         * <li>{@link #METADATA_KEY_RATING}</li>
         * <li>{@link #METADATA_KEY_USER_RATING}</li>
         * </ul>
         *
         * @param key The key for referencing this value
         * @param value The Rating value to store
         * @return The Builder to allow chaining
         */
        public Builder putRating(String key, RatingCompat value) {
            if (METADATA_KEYS_TYPE.containsKey(key)) {
                if (METADATA_KEYS_TYPE.get(key) != METADATA_TYPE_RATING) {
                    throw new IllegalArgumentException("The " + key
                            + " key cannot be used to put a Rating");
                }
            }
            mBundle.putParcelable(key, value);
            return this;
        }

        /**
         * Put a {@link Bitmap} into the metadata. Custom keys may be used, but
         * if the METADATA_KEYs defined in this class are used they may only be
         * one of the following:
         * <ul>
         * <li>{@link #METADATA_KEY_ART}</li>
         * <li>{@link #METADATA_KEY_ALBUM_ART}</li>
         * <li>{@link #METADATA_KEY_DISPLAY_ICON}</li>
         * </ul>
         *
         * @param key The key for referencing this value
         * @param value The Bitmap to store
         * @return The Builder to allow chaining
         */
        public Builder putBitmap(String key, Bitmap value) {
            if (METADATA_KEYS_TYPE.containsKey(key)) {
                if (METADATA_KEYS_TYPE.get(key) != METADATA_TYPE_BITMAP) {
                    throw new IllegalArgumentException("The " + key
                            + " key cannot be used to put a Bitmap");
                }
            }
            mBundle.putParcelable(key, value);
            return this;
        }

        /**
         * Creates a {@link MediaMetadataCompat} instance with the specified fields.
         *
         * @return The new MediaMetadata instance
         */
        public MediaMetadataCompat build() {
            return new MediaMetadataCompat(mBundle);
        }
    }
}