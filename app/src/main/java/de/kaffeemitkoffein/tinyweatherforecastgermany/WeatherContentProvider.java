/**
 * This file is part of TinyWeatherForecastGermany.
 *
 * Copyright (c) 2020, 2021, 2022, 2023 Pawel Dube
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.kaffeemitkoffein.tinyweatherforecastgermany;

import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class WeatherContentProvider extends ContentProvider {

    static final String AUTHORITY = "de.kaffeemitkoffein.tinyweatherforecastgermany";

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "weather";
    private SQLiteDatabase database;

    public static final String TABLE_NAME_FORECASTS = "forecasts";
    public static final String TABLE_NAME_WARNINGS  = "warnings";
    public static final String TABLE_NAME_TEXTS     = "texts";
    public static final String TABLE_NAME_AREAS     = "areas";

    public static final String EXISTCLAUSE = " IF NOT EXISTS ";

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int URICODE_FORECAST_SINGLE = 10;
    private static final int URICODE_FORECAST_ALL    = 11;
    private static final int URICODE_WARNING_SINGLE  = 20;
    private static final int URICODE_WARNING_ALL     = 21;
    private static final int URICODE_TEXT_SINGLE     = 30;
    private static final int URICODE_TEXT_ALL        = 31;
    private static final int URICODE_AREA_SINGLE     = 40;
    private static final int URICODE_AREA_ALL        = 41;

    static {
        uriMatcher.addURI(AUTHORITY, TABLE_NAME_FORECASTS, URICODE_FORECAST_SINGLE);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME_FORECASTS+"/*", URICODE_FORECAST_ALL);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME_WARNINGS, URICODE_WARNING_SINGLE);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME_WARNINGS+"/*", URICODE_WARNING_ALL);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME_TEXTS, URICODE_TEXT_SINGLE);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME_TEXTS+"/*", URICODE_TEXT_ALL);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME_AREAS, URICODE_AREA_SINGLE);
        uriMatcher.addURI(AUTHORITY, TABLE_NAME_AREAS+"/*", URICODE_AREA_ALL);
    }


    @Override
    public boolean onCreate() {
        Context context = getContext();
        WeatherDatabaseHelper weatherDatabaseHelper = new WeatherDatabaseHelper(context);
        database = weatherDatabaseHelper.getWritableDatabase();
        if (database!=null){
            return true;
        }
        return false;
    }

    private String getTablenameFromUri(Uri uri) throws IllegalArgumentException{
        String tableName="";
        switch (uriMatcher.match(uri)){
            case URICODE_FORECAST_SINGLE:
            case URICODE_FORECAST_ALL   :
                tableName = TABLE_NAME_FORECASTS; break;
            case URICODE_WARNING_SINGLE :
            case URICODE_WARNING_ALL    :
                tableName=TABLE_NAME_WARNINGS; break;
            case URICODE_TEXT_SINGLE    :
            case URICODE_TEXT_ALL       :
                tableName=TABLE_NAME_TEXTS; break;
            case URICODE_AREA_SINGLE    :
            case URICODE_AREA_ALL       :
                tableName=TABLE_NAME_AREAS; break;
            default: throw new IllegalArgumentException("Unknown Uri: "+uri);
        }
        return tableName;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) throws IllegalArgumentException{
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(getTablenameFromUri(uri));
        Cursor c = qb.query(database,projection,selection,selectionArgs,null,null,sortOrder);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) throws IllegalArgumentException{
        String tableName=getTablenameFromUri(uri);
        long rowId = database.insert(tableName,null,contentValues);
        // generate single item uri
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(AUTHORITY);
        switch (uriMatcher.match(uri)){
            case URICODE_FORECAST_SINGLE:
            case URICODE_FORECAST_ALL   :
                uriBuilder.appendPath(TABLE_NAME_FORECASTS); break;
            case URICODE_WARNING_SINGLE :
            case URICODE_WARNING_ALL    :
                uriBuilder.appendPath(TABLE_NAME_WARNINGS); break;
            case URICODE_TEXT_SINGLE    :
            case URICODE_TEXT_ALL       :
                uriBuilder.appendPath(TABLE_NAME_TEXTS); break;
            case URICODE_AREA_SINGLE    :
            case URICODE_AREA_ALL       :
                uriBuilder.appendPath(TABLE_NAME_AREAS); break;
            default: throw new IllegalArgumentException("Unknown Uri: "+uri);
        }
        Uri uriResult = uriBuilder.build();
        uriResult = ContentUris.withAppendedId(uriResult,rowId);
        return uriResult;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String tableName=getTablenameFromUri(uri);
        int numberDeleted = database.delete(tableName,selection,selectionArgs);
        return numberDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        String tableName=getTablenameFromUri(uri);
        int numberUpdated = database.update(tableName,contentValues,selection,selectionArgs);
        return numberUpdated;
    }

    public static class WeatherDatabaseHelper extends SQLiteOpenHelper {

        public static final String SQL_COMMAND_DROP_TABLE_FORECASTS = "DROP TABLE IF EXISTS " + TABLE_NAME_FORECASTS;
        public static final String SQL_COMMAND_DROP_TABLE_WARNINGS  = "DROP TABLE IF EXISTS " + TABLE_NAME_WARNINGS;
        public static final String SQL_COMMAND_DROP_TABLE_TEXTS     = "DROP TABLE IF EXISTS " + TABLE_NAME_TEXTS;
        public static final String SQL_COMMAND_DROP_TABLE_AREAS     = "DROP TABLE IF EXISTS " + TABLE_NAME_AREAS;

        /*
         * SQL Data for the Forecasts
         */

        public static final String KEY_FORECASTS_id="id";
        public static final String KEY_FORECASTS_timetext="timetext";
        public static final String KEY_FORECASTS_timestamp="timestamp";
        public static final String KEY_FORECASTS_name="name";
        public static final String KEY_FORECASTS_description="description";
        public static final String KEY_FORECASTS_longitude="longitude";
        public static final String KEY_FORECASTS_latitude="latitude";
        public static final String KEY_FORECASTS_altitude="altitude";
        public static final String KEY_FORECASTS_polling_time="polling_time";
        public static final String KEY_FORECASTS_elements="elements";
        public static final String KEY_FORECASTS_timesteps="timesteps";
        public static final String KEY_FORECASTS_TTT="TTT";
        public static final String KEY_FORECASTS_E_TTT="E_TTT";
        public static final String KEY_FORECASTS_T5cm="T5cm";
        public static final String KEY_FORECASTS_Td="Td";
        public static final String KEY_FORECASTS_E_Td="E_Td";
        public static final String KEY_FORECASTS_Tx="Tx";
        public static final String KEY_FORECASTS_Tn="Tn";
        public static final String KEY_FORECASTS_TM="TM";
        public static final String KEY_FORECASTS_TG="TG";
        public static final String KEY_FORECASTS_DD="DD";
        public static final String KEY_FORECASTS_E_DD="E_DD";
        public static final String KEY_FORECASTS_FF="FF";
        public static final String KEY_FORECASTS_E_FF="E_FF";
        public static final String KEY_FORECASTS_FX1="FX1";
        public static final String KEY_FORECASTS_FX3="FX3";
        public static final String KEY_FORECASTS_FXh="FXh";
        public static final String KEY_FORECASTS_FXh25="FXh25";
        public static final String KEY_FORECASTS_FXh40="FXh40";
        public static final String KEY_FORECASTS_FXh55="FXh55";
        public static final String KEY_FORECASTS_FX625="FX625";
        public static final String KEY_FORECASTS_FX640="FX640";
        public static final String KEY_FORECASTS_FX655="FX655";
        public static final String KEY_FORECASTS_RR1="RR1";
        public static final String KEY_FORECASTS_RR1c="RR1c";
        public static final String KEY_FORECASTS_RRL1c="RRL1c";
        public static final String KEY_FORECASTS_RR3="RR3";
        public static final String KEY_FORECASTS_RR6="RR6";
        public static final String KEY_FORECASTS_RR3c="RR3c";
        public static final String KEY_FORECASTS_RR6c="RR6c";
        public static final String KEY_FORECASTS_RRhc="RRhc";
        public static final String KEY_FORECASTS_RRdc="RRdc";
        public static final String KEY_FORECASTS_RRS1c="RRS1c";
        public static final String KEY_FORECASTS_RRS3c="RRS3c";
        public static final String KEY_FORECASTS_R101="R101";
        public static final String KEY_FORECASTS_R102="R102";
        public static final String KEY_FORECASTS_R103="R103";
        public static final String KEY_FORECASTS_R105="R105";
        public static final String KEY_FORECASTS_R107="R107";
        public static final String KEY_FORECASTS_R110="R110";
        public static final String KEY_FORECASTS_R120="R120";
        public static final String KEY_FORECASTS_R130="R130";
        public static final String KEY_FORECASTS_R150="R150";
        public static final String KEY_FORECASTS_RR1o1="RR1o1";
        public static final String KEY_FORECASTS_RR1w1="RR1w1";
        public static final String KEY_FORECASTS_RR1u1="RR1u1";
        public static final String KEY_FORECASTS_R600="R600";
        public static final String KEY_FORECASTS_Rh00="Rh00";
        public static final String KEY_FORECASTS_R602="R602";
        public static final String KEY_FORECASTS_Rh02="Rh02";
        public static final String KEY_FORECASTS_Rd02="Rd02";
        public static final String KEY_FORECASTS_R610="R610";
        public static final String KEY_FORECASTS_Rh10="Rh10";
        public static final String KEY_FORECASTS_R650="R650";
        public static final String KEY_FORECASTS_Rh50="Rh50";
        public static final String KEY_FORECASTS_Rd00="Rd00";
        public static final String KEY_FORECASTS_Rd10="Rd10";
        public static final String KEY_FORECASTS_Rd50="Rd50";
        public static final String KEY_FORECASTS_wwPd="wwPd";
        public static final String KEY_FORECASTS_DRR1="DRR1";
        public static final String KEY_FORECASTS_wwZ="wwZ";
        public static final String KEY_FORECASTS_wwZ6="wwZ6";
        public static final String KEY_FORECASTS_wwZh="wwZh";
        public static final String KEY_FORECASTS_wwD="wwD";
        public static final String KEY_FORECASTS_wwD6="wwD6";
        public static final String KEY_FORECASTS_wwDh="wwDh";
        public static final String KEY_FORECASTS_wwC="wwC";
        public static final String KEY_FORECASTS_wwC6="wwC6";
        public static final String KEY_FORECASTS_wwCh="wwCh";
        public static final String KEY_FORECASTS_wwT="wwT";
        public static final String KEY_FORECASTS_wwT6="wwT6";
        public static final String KEY_FORECASTS_wwTh="wwTh";
        public static final String KEY_FORECASTS_wwTd="wwTd";
        public static final String KEY_FORECASTS_wwL="wwL";
        public static final String KEY_FORECASTS_wwL6="wwL6";
        public static final String KEY_FORECASTS_wwLh="wwLh";
        public static final String KEY_FORECASTS_wwS="wwS";
        public static final String KEY_FORECASTS_wwS6="wwS6";
        public static final String KEY_FORECASTS_wwSh="wwSh";
        public static final String KEY_FORECASTS_wwF="wwF";
        public static final String KEY_FORECASTS_wwF6="wwF6";
        public static final String KEY_FORECASTS_wwFh="wwFh";
        public static final String KEY_FORECASTS_wwP="wwP";
        public static final String KEY_FORECASTS_wwP6="wwP6";
        public static final String KEY_FORECASTS_wwPh="wwPh";
        public static final String KEY_FORECASTS_VV10="VV10";
        public static final String KEY_FORECASTS_ww="ww";
        public static final String KEY_FORECASTS_ww3="ww3";
        public static final String KEY_FORECASTS_W1W2="W1W2";
        public static final String KEY_FORECASTS_WPc11="WPc11";
        public static final String KEY_FORECASTS_WPc31="WPc31";
        public static final String KEY_FORECASTS_WPc61="WPc61";
        public static final String KEY_FORECASTS_WPch1="WPch1";
        public static final String KEY_FORECASTS_WPcd1="WPcd1";
        public static final String KEY_FORECASTS_N="N";
        public static final String KEY_FORECASTS_Neff="Neff";
        public static final String KEY_FORECASTS_N05="N05";
        public static final String KEY_FORECASTS_Nl="Nl";
        public static final String KEY_FORECASTS_Nm="Nm";
        public static final String KEY_FORECASTS_Nh="Nh";
        public static final String KEY_FORECASTS_Nlm="Nlm";
        public static final String KEY_FORECASTS_H_BsC="H_BsC";
        public static final String KEY_FORECASTS_PPPP="PPPP";
        public static final String KEY_FORECASTS_E_PPP="E_PPP";
        public static final String KEY_FORECASTS_RadS1="RadS1";
        public static final String KEY_FORECASTS_RadS3="RadS3";
        public static final String KEY_FORECASTS_RRad1="RRad1";
        public static final String KEY_FORECASTS_Rad1h="Rad1h";
        public static final String KEY_FORECASTS_RadL3="RadL3";
        public static final String KEY_FORECASTS_VV="VV";
        public static final String KEY_FORECASTS_D1="D1";
        public static final String KEY_FORECASTS_SunD="SunD";
        public static final String KEY_FORECASTS_SunD3="SunD3";
        public static final String KEY_FORECASTS_RSunD="RSunD";
        public static final String KEY_FORECASTS_PSd00="PSd00";
        public static final String KEY_FORECASTS_PSd30="PSd30";
        public static final String KEY_FORECASTS_PSd60="PSd60";
        public static final String KEY_FORECASTS_wwM="wwM";
        public static final String KEY_FORECASTS_wwM6="wwM6";
        public static final String KEY_FORECASTS_wwMh="wwMh";
        public static final String KEY_FORECASTS_wwMd="wwMd";
        public static final String KEY_FORECASTS_PEvap="PEvap";

        public static final String SQL_COMMAND_CREATE_TABLE_FORECASTS = "CREATE TABLE " + EXISTCLAUSE + TABLE_NAME_FORECASTS + "("
                + KEY_FORECASTS_id + " INTEGER PRIMARY KEY ASC,"
                + KEY_FORECASTS_timetext + " TEXT,"
                + KEY_FORECASTS_name + " TEXT,"
                + KEY_FORECASTS_description + " TEXT,"
                + KEY_FORECASTS_longitude + " REAL,"
                + KEY_FORECASTS_latitude + " REAL,"
                + KEY_FORECASTS_altitude + " REAL,"
                + KEY_FORECASTS_timesteps + " TEXT,"
                + KEY_FORECASTS_TTT + " TEXT,"
                + KEY_FORECASTS_E_TTT + " TEXT,"
                + KEY_FORECASTS_T5cm + " TEXT,"
                + KEY_FORECASTS_Td + " TEXT,"
                + KEY_FORECASTS_E_Td + " TEXT,"
                + KEY_FORECASTS_Tx + " TEXT,"
                + KEY_FORECASTS_Tn + " TEXT,"
                + KEY_FORECASTS_TM + " TEXT,"
                + KEY_FORECASTS_TG + " TEXT,"
                + KEY_FORECASTS_DD + " TEXT,"
                + KEY_FORECASTS_E_DD + " TEXT,"
                + KEY_FORECASTS_FF + " TEXT,"
                + KEY_FORECASTS_E_FF + " TEXT,"
                + KEY_FORECASTS_FX1 + " TEXT,"
                + KEY_FORECASTS_FX3 + " TEXT,"
                + KEY_FORECASTS_FXh + " TEXT,"
                + KEY_FORECASTS_FXh25 + " TEXT,"
                + KEY_FORECASTS_FXh40 + " TEXT,"
                + KEY_FORECASTS_FXh55 + " TEXT,"
                + KEY_FORECASTS_FX625 + " TEXT,"
                + KEY_FORECASTS_FX640 + " TEXT,"
                + KEY_FORECASTS_FX655 + " TEXT,"
                + KEY_FORECASTS_RR1 + " TEXT,"
                + KEY_FORECASTS_RR1c + " TEXT,"
                + KEY_FORECASTS_RRL1c + " TEXT,"
                + KEY_FORECASTS_RR3 + " TEXT,"
                + KEY_FORECASTS_RR6 + " TEXT,"
                + KEY_FORECASTS_RR3c + " TEXT,"
                + KEY_FORECASTS_RR6c + " TEXT,"
                + KEY_FORECASTS_RRhc + " TEXT,"
                + KEY_FORECASTS_RRdc + " TEXT,"
                + KEY_FORECASTS_RRS1c + " TEXT,"
                + KEY_FORECASTS_RRS3c + " TEXT,"
                + KEY_FORECASTS_R101 + " TEXT,"
                + KEY_FORECASTS_R102 + " TEXT,"
                + KEY_FORECASTS_R103 + " TEXT,"
                + KEY_FORECASTS_R105 + " TEXT,"
                + KEY_FORECASTS_R107 + " TEXT,"
                + KEY_FORECASTS_R110 + " TEXT,"
                + KEY_FORECASTS_R120 + " TEXT,"
                + KEY_FORECASTS_R130 + " TEXT,"
                + KEY_FORECASTS_R150 + " TEXT,"
                + KEY_FORECASTS_RR1o1 + " TEXT,"
                + KEY_FORECASTS_RR1w1 + " TEXT,"
                + KEY_FORECASTS_RR1u1 + " TEXT,"
                + KEY_FORECASTS_R600 + " TEXT,"
                + KEY_FORECASTS_Rh00 + " TEXT,"
                + KEY_FORECASTS_R602 + " TEXT,"
                + KEY_FORECASTS_Rh02 + " TEXT,"
                + KEY_FORECASTS_Rd02 + " TEXT,"
                + KEY_FORECASTS_R610 + " TEXT,"
                + KEY_FORECASTS_Rh10 + " TEXT,"
                + KEY_FORECASTS_R650 + " TEXT,"
                + KEY_FORECASTS_Rh50 + " TEXT,"
                + KEY_FORECASTS_Rd00 + " TEXT,"
                + KEY_FORECASTS_Rd10 + " TEXT,"
                + KEY_FORECASTS_Rd50 + " TEXT,"
                + KEY_FORECASTS_wwPd + " TEXT,"
                + KEY_FORECASTS_DRR1 + " TEXT,"
                + KEY_FORECASTS_wwZ + " TEXT,"
                + KEY_FORECASTS_wwZ6 + " TEXT,"
                + KEY_FORECASTS_wwZh + " TEXT,"
                + KEY_FORECASTS_wwD + " TEXT,"
                + KEY_FORECASTS_wwD6 + " TEXT,"
                + KEY_FORECASTS_wwDh + " TEXT,"
                + KEY_FORECASTS_wwC + " TEXT,"
                + KEY_FORECASTS_wwC6 + " TEXT,"
                + KEY_FORECASTS_wwCh + " TEXT,"
                + KEY_FORECASTS_wwT + " TEXT,"
                + KEY_FORECASTS_wwT6 + " TEXT,"
                + KEY_FORECASTS_wwTh + " TEXT,"
                + KEY_FORECASTS_wwTd + " TEXT,"
                + KEY_FORECASTS_wwL + " TEXT,"
                + KEY_FORECASTS_wwL6 + " TEXT,"
                + KEY_FORECASTS_wwLh + " TEXT,"
                + KEY_FORECASTS_wwS + " TEXT,"
                + KEY_FORECASTS_wwS6 + " TEXT,"
                + KEY_FORECASTS_wwSh + " TEXT,"
                + KEY_FORECASTS_wwF + " TEXT,"
                + KEY_FORECASTS_wwF6 + " TEXT,"
                + KEY_FORECASTS_wwFh + " TEXT,"
                + KEY_FORECASTS_wwP + " TEXT,"
                + KEY_FORECASTS_wwP6 + " TEXT,"
                + KEY_FORECASTS_wwPh + " TEXT,"
                + KEY_FORECASTS_VV10 + " TEXT,"
                + KEY_FORECASTS_ww + " TEXT,"
                + KEY_FORECASTS_ww3 + " TEXT,"
                + KEY_FORECASTS_W1W2 + " TEXT,"
                + KEY_FORECASTS_WPc11 + " TEXT,"
                + KEY_FORECASTS_WPc31 + " TEXT,"
                + KEY_FORECASTS_WPc61 + " TEXT,"
                + KEY_FORECASTS_WPch1 + " TEXT,"
                + KEY_FORECASTS_WPcd1 + " TEXT,"
                + KEY_FORECASTS_N + " TEXT,"
                + KEY_FORECASTS_Neff + " TEXT,"
                + KEY_FORECASTS_N05 + " TEXT,"
                + KEY_FORECASTS_Nl + " TEXT,"
                + KEY_FORECASTS_Nm + " TEXT,"
                + KEY_FORECASTS_Nh + " TEXT,"
                + KEY_FORECASTS_Nlm + " TEXT,"
                + KEY_FORECASTS_H_BsC + " TEXT,"
                + KEY_FORECASTS_PPPP + " TEXT,"
                + KEY_FORECASTS_E_PPP + " TEXT,"
                + KEY_FORECASTS_RadS1 + " TEXT,"
                + KEY_FORECASTS_RadS3 + " TEXT,"
                + KEY_FORECASTS_RRad1 + " TEXT,"
                + KEY_FORECASTS_Rad1h + " TEXT,"
                + KEY_FORECASTS_RadL3 + " TEXT,"
                + KEY_FORECASTS_VV + " TEXT,"
                + KEY_FORECASTS_D1 + " TEXT,"
                + KEY_FORECASTS_SunD + " TEXT,"
                + KEY_FORECASTS_SunD3 + " TEXT,"
                + KEY_FORECASTS_RSunD + " TEXT,"
                + KEY_FORECASTS_PSd00 + " TEXT,"
                + KEY_FORECASTS_PSd30 + " TEXT,"
                + KEY_FORECASTS_PSd60 + " TEXT,"
                + KEY_FORECASTS_wwM + " TEXT,"
                + KEY_FORECASTS_wwM6 + " TEXT,"
                + KEY_FORECASTS_wwMh + " TEXT,"
                + KEY_FORECASTS_wwMd + " TEXT,"
                + KEY_FORECASTS_PEvap + " TEXT,"
                + KEY_FORECASTS_timestamp + " INTEGER,"
                + KEY_FORECASTS_polling_time + " INTEGER,"
                + KEY_FORECASTS_elements + " INTEGER" + "" + ");";

        /*
         * SQL Data for the Warnings
         */

        public static final String KEY_WARNINGS_id = "id";
        public static final String KEY_WARNINGS_polling_time = "polling_time";
        public static final String KEY_WARNINGS_identifier = "identifier";
        public static final String KEY_WARNINGS_sender = "sender";
        public static final String KEY_WARNINGS_sent = "sent";
        public static final String KEY_WARNINGS_status = "status";
        public static final String KEY_WARNINGS_msgType = "msgType";
        public static final String KEY_WARNINGS_source = "source";
        public static final String KEY_WARNINGS_scope = "scope";
        public static final String KEY_WARNINGS_codes = "codes";
        public static final String KEY_WARNINGS_references = "reference_key";
        public static final String KEY_WARNINGS_language = "language";
        public static final String KEY_WARNINGS_category = "category";
        public static final String KEY_WARNINGS_event = "event";
        public static final String KEY_WARNINGS_responseType = "responseType";
        public static final String KEY_WARNINGS_urgency = "urgency";
        public static final String KEY_WARNINGS_severity = "severity";
        public static final String KEY_WARNINGS_certainty = "certainty";
        public static final String KEY_WARNINGS_effective = "effective";
        public static final String KEY_WARNINGS_onset = "onset";
        public static final String KEY_WARNINGS_expires = "expires";
        public static final String KEY_WARNINGS_senderName = "senderName";
        public static final String KEY_WARNINGS_headline = "headline";
        public static final String KEY_WARNINGS_description = "description";
        public static final String KEY_WARNINGS_instruction = "instruction";
        public static final String KEY_WARNINGS_web = "web";
        public static final String KEY_WARNINGS_contact = "contact";
        public static final String KEY_WARNINGS_profile_version = "profile_version";
        public static final String KEY_WARNINGS_license = "license";
        public static final String KEY_WARNINGS_ii = "ii";
        public static final String KEY_WARNINGS_groups = "groups";
        public static final String KEY_WARNINGS_area_color = "area_color";
        public static final String KEY_WARNINGS_parameter_names = "parameter_names";
        public static final String KEY_WARNINGS_parameter_values = "parameter_values";
        public static final String KEY_WARNINGS_polygons = "polygons";
        public static final String KEY_WARNINGS_excluded_polygons = "excluded_polygons";
        public static final String KEY_WARNINGS_area_names = "area_names";
        public static final String KEY_WARNINGS_area_warncellIDs = "area_warncellIDs";

        public static final String SQL_COMMAND_CREATE_TABLE_WARNINGS = "CREATE TABLE " + EXISTCLAUSE + TABLE_NAME_WARNINGS + "("
                + KEY_WARNINGS_id + " INTEGER PRIMARY KEY ASC,"
                + KEY_WARNINGS_polling_time + " INTEGER,"
                + KEY_WARNINGS_identifier + " TEXT,"
                + KEY_WARNINGS_sender + " TEXT,"
                + KEY_WARNINGS_sent + " INTEGER,"
                + KEY_WARNINGS_status + " TEXT,"
                + KEY_WARNINGS_msgType + " TEXT,"
                + KEY_WARNINGS_source + " TEXT,"
                + KEY_WARNINGS_scope + " TEXT,"
                + KEY_WARNINGS_codes + " TEXT,"
                + KEY_WARNINGS_references + " TEXT,"
                + KEY_WARNINGS_language + " TEXT,"
                + KEY_WARNINGS_category + " TEXT,"
                + KEY_WARNINGS_event + " TEXT,"
                + KEY_WARNINGS_responseType + " TEXT,"
                + KEY_WARNINGS_urgency + " TEXT,"
                + KEY_WARNINGS_severity + " TEXT,"
                + KEY_WARNINGS_certainty + " TEXT,"
                + KEY_WARNINGS_effective + " INTEGER,"
                + KEY_WARNINGS_onset + " INTEGER,"
                + KEY_WARNINGS_expires + " INTEGER,"
                + KEY_WARNINGS_senderName + " TEXT,"
                + KEY_WARNINGS_headline + " TEXT,"
                + KEY_WARNINGS_description + " TEXT,"
                + KEY_WARNINGS_instruction + " TEXT,"
                + KEY_WARNINGS_web + " TEXT,"
                + KEY_WARNINGS_contact + " TEXT,"
                + KEY_WARNINGS_profile_version + " TEXT,"
                + KEY_WARNINGS_license + " TEXT,"
                + KEY_WARNINGS_ii + " TEXT,"
                + KEY_WARNINGS_groups + " TEXT,"
                + KEY_WARNINGS_area_color + " TEXT,"
                + KEY_WARNINGS_parameter_names + " TEXT,"
                + KEY_WARNINGS_parameter_values + " TEXT,"
                + KEY_WARNINGS_polygons + " TEXT,"
                + KEY_WARNINGS_excluded_polygons + " TEXT,"
                + KEY_WARNINGS_area_names + " TEXT,"
                + KEY_WARNINGS_area_warncellIDs + " TEXT" + ");";
        /*
         * SQL Data for the Texts
         */

        public static final String KEY_TEXTS_id = "id";
        public static final String KEY_TEXTS_identifier = "identifier";
        public static final String KEY_TEXTS_weburl = "weburl";
        public static final String KEY_TEXTS_content = "content";
        public static final String KEY_TEXTS_title = "title";
        public static final String KEY_TEXTS_subtitle = "subtitle";
        public static final String KEY_TEXTS_issued_text = "issued_text";
        public static final String KEY_TEXTS_type = "type";
        public static final String KEY_TEXTS_issued = "issued";
        public static final String KEY_TEXTS_polled = "polled";
        public static final String KEY_TEXTS_outdated = "outdated";

        public static final String SQL_COMMAND_CREATE_TABLE_TEXTS = "CREATE TABLE " + EXISTCLAUSE + TABLE_NAME_TEXTS + "("
                + KEY_TEXTS_id + " INTEGER PRIMARY KEY ASC,"
                + KEY_TEXTS_identifier + " TEXT,"
                + KEY_TEXTS_weburl + " TEXT,"
                + KEY_TEXTS_content + " TEXT,"
                + KEY_TEXTS_title + " TEXT,"
                + KEY_TEXTS_subtitle + " TEXT,"
                + KEY_TEXTS_issued_text + " TEXT,"
                + KEY_TEXTS_type + " INTEGER,"
                + KEY_TEXTS_issued + " INTEGER,"
                + KEY_TEXTS_polled+ " INTEGER,"
                + KEY_TEXTS_outdated + " INTEGER"
                + ");";

        /*
         * SQL Data for the Areas
         */

        public static final String KEY_AREAS_id = "id";
        public static final String KEY_AREAS_warncellid = "warncellid";
        public static final String KEY_AREAS_warncenter = "warncenter";
        public static final String KEY_AREAS_type = "type";
        public static final String KEY_AREAS_name = "name";
        public static final String KEY_AREAS_polygonstring = "polygonstring";

        public static final String SQL_COMMAND_CREATE_TABLE_AREAS = "CREATE TABLE " + EXISTCLAUSE + TABLE_NAME_AREAS + "("
                + KEY_AREAS_id + " INTEGER PRIMARY KEY ASC,"
                + KEY_AREAS_warncellid + " TEXT,"
                + KEY_AREAS_warncenter + " TEXT,"
                + KEY_AREAS_type + " INTEGER,"
                + KEY_AREAS_name + " TEXT,"
                + KEY_AREAS_polygonstring + " TEXT"
                + ");";

        public WeatherDatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(SQL_COMMAND_CREATE_TABLE_FORECASTS);
            sqLiteDatabase.execSQL(SQL_COMMAND_CREATE_TABLE_WARNINGS);
            sqLiteDatabase.execSQL(SQL_COMMAND_CREATE_TABLE_TEXTS);
            sqLiteDatabase.execSQL(SQL_COMMAND_CREATE_TABLE_AREAS);
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            setWriteAheadLoggingEnabled(true);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            // change from version 2 to 3: only forecast data needs to be dropped because of new fields in data set
            if ((oldVersion==2) && (newVersion==3)){
                sqLiteDatabase.execSQL(SQL_COMMAND_DROP_TABLE_FORECASTS);
                onCreate(sqLiteDatabase);
            } else {
                // all other database upgrades require complete reset
                // drop data & re-create tables
                sqLiteDatabase.execSQL(SQL_COMMAND_DROP_TABLE_FORECASTS);
                sqLiteDatabase.execSQL(SQL_COMMAND_DROP_TABLE_WARNINGS);
                sqLiteDatabase.execSQL(SQL_COMMAND_DROP_TABLE_TEXTS);
                sqLiteDatabase.execSQL(SQL_COMMAND_DROP_TABLE_AREAS);
                onCreate(sqLiteDatabase);
            }
        }

        @Override
        public void onDowngrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            // all other database upgrades require complete reset
            // drop data & re-create tables
            sqLiteDatabase.execSQL(SQL_COMMAND_DROP_TABLE_FORECASTS);
            sqLiteDatabase.execSQL(SQL_COMMAND_DROP_TABLE_WARNINGS);
            sqLiteDatabase.execSQL(SQL_COMMAND_DROP_TABLE_TEXTS);
            sqLiteDatabase.execSQL(SQL_COMMAND_DROP_TABLE_AREAS);
            onCreate(sqLiteDatabase);
            //super.onDowngrade(db, oldVersion, newVersion);
        }
    }

}
