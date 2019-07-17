package com.example.x190629.testes_geofence.cache;

import android.location.Location;package wit.android.bcpBankingApp.cache.handlers;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

import wit.android.bcpBankingApp.core.xml.CDAAccountDetailOutput;
import wit.android.bcpBankingApp.core.xml.CommonServiceOutput;
import wit.android.bcpBankingApp.core.xml.ResultStatus;
import wit.android.bcpBankingApp.entities.TimeAccountDetailsData;
import wit.android.bcpBankingApp.utils.CryptUtils;
import wit.android.bcpBankingApp.utils.DBUtils;
import wit.android.bcpBankingApp.utils.L;
import wit.android.bcpBankingApp.utils.StringUtils;
/**
 * Created by X191104 on 7/15/2019.
 */

public class LocationCacheHandler extends CacheHandler{


    // Constants ------------------------------------------------

    /** The tag prefix for logging. */
    private static final String TAG = "LocationCacheHandler";

    public static final String LOCATION_ID = "_id";

    //-----------------  LOCATION TABLE -----------------------

    public static final String LOCATION_TABLE_NAME = "location";

    /** Id column name */
    public static final String COL_LAST_LAT = "Lastlatitude";

    public static final String COL_LAST_LONG = "Lastlongitude";
    /** Serializable data column name */
    public static final String COL_PUSH_SENT = "pushSent";

    public static final String LOCATION_COLUMNS = "(" + LOCATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COL_LAST_LAT + " DOUBLE," + COL_LAST_LONG + " DOUBLE,"
            + COL_PUSH_SENT + " BOOLEAN"
            + ")";

    /** Script for the table creation */
    public static final String CREATE_LEGACY_TIME_DETAILS_TABLE_SQL = "CREATE TABLE " + LOCATION_TABLE_NAME + LOCATION_COLUMNS;



    public LocationCacheHandler(Context ctx, SQLiteDatabase db, String encryptionSeed) {
        super(ctx, db, encryptionSeed);

    }


    private long insertLocationDetails(double lastLat, double lastLong, boolean onAirport, boolean pushSent) throws Exception {
        ContentValues values = new ContentValues();
        CryptUtils.addEncryptedValue(COL_LAST_LAT, BackgroundService.getLastLat(), encryptionSeed, values);
        CryptUtils.addEncryptedValue(COL_LAST_LONG, BackgroundService.getLastLong(), encryptionSeed, values);
        CryptUtils.addEncryptedValue(COL_PUSH_SENT, BackgroundService.getPushSent(), encryptionSeed, values);
        return db.insert(LOCATION_TABLE_NAME, LOCATION_ID, values);
    }


    private long updateLocationDetails(double lastLat, double lastLong, boolean onAirport, boolean pushSent) throws Exception {
        ContentValues values = new ContentValues();
        values.put(COL_LAST_LAT, lastLat);
        values.put(COL_LAST_LONG, lastLong);
        values.put(COL_PUSH_SENT, pushSent);

        String whereClause = LOCATION_ID + "=0";
        return db.insert(LOCATION_TABLE_NAME, LOCATION_ID, values, whereClause, null);
    }

    private Location getLocation() {
        Location lastLocation = new Location("myProvider");
        String whereClause, whereClause2;
        Cursor cursor = null;
        try {
            if (LOCATION_ID == null) {
                whereClause = null;
                whereClause2 = null;
            } else {
                whereClause = COL_LAST_LAT + "=?";
                whereClause2 = COL_LAST_LONG + "=?";
                double lat = db.query(LOCATION_TABLE_NAME, null, whereClause,null, null, null, null);
                double longi = db.query(LOCATION_TABLE_NAME, null, whereClause2, null, null, null, null);

                lastLocation.setLatitude(lat);
                lastLocation.setLatitude(longi);
            }


        } catch (Exception ex) {
            L.e(TAG, "getTimeAccountsDetails error getting time account(s) details accUID=" + accUID, ex);
        }


        return lastLocation ;

        }



    private boolean getPushSent() {
        boolean pushSent = false;
        String whereClause;
        Cursor cursor = null;
        try {
            if (LOCATION_ID == null) {
                whereClause = null;
            } else {
                whereClause = COL_ON_AIRPORT + "=?";
                pushSent = db.query(LOCATION_TABLE_NAME, null, whereClause,null, null, null, null);
            }


        } catch (Exception ex) {
            L.e(TAG, "getTimeAccountsDetails error getting time account(s) details accUID=" + accUID, ex);
        }


        return pushSent ;

    }




        /**
         * Parses a time account details data from the received cursor from DB.
         *
         * @param cursor
         *        The cursor from DB.
         * @return The created time account details data object.
         * @throws Exception
         *         If some error occurs.
         */
    private TimeAccountDetailsData parseTimeAccountDetails(Cursor cursor) throws Exception {
        String id = cursor.getString(cursor.getColumnIndex(COL_ID));
        String accountUID = cursor.getString(cursor.getColumnIndex(COL_UID));
        String name = CryptUtils.decryptString(cursor.getBlob(cursor.getColumnIndex(COL_NAME)), encryptionSeed);
        String relatedDDAAccountNumber =
                CryptUtils.decryptString(cursor.getBlob(cursor.getColumnIndex(COL_RELATED_DDA_ACCOUNT_NUMBER)),
                        encryptionSeed);
        String openingDate =
                CryptUtils.decryptString(cursor.getBlob(cursor.getColumnIndex(COL_OPENING_DATE)), encryptionSeed);
        String maturity = CryptUtils.decryptString(cursor.getBlob(cursor.getColumnIndex(COL_MATURITY)), encryptionSeed);
        Double accountBalance =
                CryptUtils.decryptDouble(cursor.getBlob(cursor.getColumnIndex(COL_ACCOUNT_BALANCE)), encryptionSeed);
        Short term = CryptUtils.decryptShort(cursor.getBlob(cursor.getColumnIndex(COL_TERM)), encryptionSeed);
        String termUnit =
                CryptUtils.decryptString(cursor.getBlob(cursor.getColumnIndex(COL_TERM_UNIT)), encryptionSeed);
        Double interestRate =
                CryptUtils.decryptDouble(cursor.getBlob(cursor.getColumnIndex(COL_INTEREST_RATE)), encryptionSeed);
        String nextInterestPayment =
                CryptUtils.decryptString(cursor.getBlob(cursor.getColumnIndex(COL_NEXT_INTEREST_PAYMENT)),
                        encryptionSeed);
        Boolean interestPaidToRelatedDDA =
                CryptUtils.decryptBoolean(cursor.getBlob(cursor.getColumnIndex(COL_INTEREST_PAID_TO_RELATED_DDA)),
                        encryptionSeed);
        Short rolloverCount =
                CryptUtils.decryptShort(cursor.getBlob(cursor.getColumnIndex(COL_ROLLOVER_COUNT)), encryptionSeed);
        Double grossInterest =
                CryptUtils.decryptDouble(cursor.getBlob(cursor.getColumnIndex(COL_GROSS_INTEREST)), encryptionSeed);
        Double taxValue =
                CryptUtils.decryptDouble(cursor.getBlob(cursor.getColumnIndex(COL_TAX_VALUE)), encryptionSeed);
        String accountDescription =
                CryptUtils
                        .decryptString(cursor.getBlob(cursor.getColumnIndex(COL_ACCOUNT_DESCRIPTION)), encryptionSeed);
        Boolean automaticRollover =
                CryptUtils
                        .decryptBoolean(cursor.getBlob(cursor.getColumnIndex(COL_AUTOMATIC_ROLLOVER)), encryptionSeed);
        String accountPlan =
                CryptUtils.decryptString(cursor.getBlob(cursor.getColumnIndex(COL_ACCOUNT_PLAN)), encryptionSeed);
        String businessArea =
                CryptUtils.decryptString(cursor.getBlob(cursor.getColumnIndex(COL_BUSINESS_AREA)), encryptionSeed);
        Double netInterest =
                CryptUtils.decryptDouble(cursor.getBlob(cursor.getColumnIndex(COL_NET_INTEREST)), encryptionSeed);

        ResultStatus rs = new ResultStatus(true, null, null);
        CommonServiceOutput cso = new CommonServiceOutput(rs, null, null, null, 0, "encryptedKey", "encryptedRequest");
        CDAAccountDetailOutput output = new CDAAccountDetailOutput(
                cso , id, name, relatedDDAAccountNumber, openingDate,
                maturity, accountBalance, term, termUnit, interestRate,
                nextInterestPayment, interestPaidToRelatedDDA,
                rolloverCount, grossInterest, taxValue, accountDescription,
                automaticRollover, accountPlan, businessArea, netInterest
        );

        String lastUpdateDate = cursor.getString(cursor.getColumnIndex(COL_LAST_UPDATE));

        return new TimeAccountDetailsData(output, lastUpdateDate, accountUID);
    }

    // public methods ---------------------------------------------------------

    /**
     * Gets all time accounts' details from database.
     *
     * @return An array of {@link TimeAccountDetailsData} with all time accounts' details from the legacy table.
     */
    public TimeAccountDetailsData[] getTimeAccountsDetailsFromLegacyTable() {
        ArrayList<TimeAccountDetailsData> details = new ArrayList<TimeAccountDetailsData>();
        Cursor cursor = null;
        try {
            if (!DBUtils.doesTableExist(LEGACY_TIME_DETAILS_TABLE, db)) {
                L.d(TAG, "getTimeAccountsDetailsFromLegacyTable legacy table does not exist, returning null");
                return null;
            }

            cursor = db.query(LEGACY_TIME_DETAILS_TABLE, null, null, null, null, null, null);
            while (cursor.moveToNext()) {
                byte[] detailsBytes = StringUtils.decrypt(cursor.getBlob(cursor.getColumnIndex(TIME_DETAILS_TABLE_DATA)), encryptionSeed);
                String lastUpdateDate = cursor.getString(cursor.getColumnIndex(TIME_DETAILS_TABLE_LAST_UPDATE_DATE));
                String accUID = cursor.getString(cursor.getColumnIndex(TIME_DETAILS_TABLE_ACCOUNT_UID_REF));

                // Deserialize object
                ObjectInputStream ois_data = new ObjectInputStream(new ByteArrayInputStream(detailsBytes));
                TimeAccountDetailsData detail = new TimeAccountDetailsData();
                detail.setOutput((CDAAccountDetailOutput) ois_data.readObject());
                detail.setLastUpdateDate(lastUpdateDate);
                detail.setAccountUID(accUID);
                details.add(detail);
            }

        } catch (Exception ex) {
            L.e(TAG, "getTimeAccountsDetailsFromLegacyTable error getting account details from legacy table",ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        TimeAccountDetailsData[] detailsArr = new TimeAccountDetailsData[details.size()];
        details.toArray(detailsArr);
        return detailsArr;
    }

    /**
     * Deletes the time accounts' details from the legacy table.
     *
     * @return The number of time accounts' details deleted from the legacy table.
     */
    public int deleteTimeDetailsFromLegacyTable() {
        try {
            if (!DBUtils.doesTableExist(LEGACY_TIME_DETAILS_TABLE, db)) {
                L.d(TAG, "deleteTimeDetailsFromLegacyTable legacy table does not exist, returning -1");
                return -1;
            }
        } catch (Exception e1) {
            L.e(TAG, "deleteTimeDetailsFromLegacyTable error checking if legacy time details table exists", e1);
        }

        int deleted = -1;
        try {
            deleted = db.delete(LEGACY_TIME_DETAILS_TABLE, "1", null);
        } catch (Exception e) {
            L.e(TAG, "deleteTimeDetailsFromLegacyTable error deleting time details from legacy table", e);
        }
        return deleted;
    }

    /**
     * Gets time account details from DB:
     * -if the received time account unique ID is not null, retrieves the details of
     * the time account with the received UID;
     * -otherwise retrieves all time account details.
     *
     * @param accUID
     *        Optional: the Unique ID of the time account whose details are wanted; if null, retrieves all time account
     *        details.
     * @return An array of {@link TimeAccountDetailsData} with the time account details from DB.
     */
    private TimeAccountDetailsData[] getTimeAccountsDetails(String accUID) {
        ArrayList<TimeAccountDetailsData> details = new ArrayList<TimeAccountDetailsData>();

        String whereClause;
        String[] whereArgs;
        Cursor cursor = null;
        try {
            if (accUID == null) {
                whereClause = null;
                whereArgs = null;
            } else {
                whereClause = COL_UID + "=?";
                whereArgs = new String[] {accUID};
            }
            cursor = db.query(TABLE_NAME, null, whereClause, whereArgs, null, null, null);
            while (cursor.moveToNext()) {
                details.add(parseTimeAccountDetails(cursor));
            }
        } catch (Exception ex) {
            L.e(TAG, "getTimeAccountsDetails error getting time account(s) details accUID=" + accUID, ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        TimeAccountDetailsData[] detailsArr = new TimeAccountDetailsData[details.size()];
        details.toArray(detailsArr);
        return detailsArr;
    }

    /**
     * Gets the number of time accounts' details in DB.
     *
     * @return The number of time accounts's details in DB.
     */
    public int getTimeAccountDetailsCount() {
        int count = -1;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("select count(*) from " + TABLE_NAME, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception ex) {
            L.e(TAG, "getTimeAccountDetailsCount error getting accounts encryptionSeed=" + encryptionSeed, ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        L.d(TAG, "getTimeAccountDetailsCount count=" + count);

        return count;
    }

    /**
     * Gets the details of the time account with the received UID.
     *
     * @param accountUniqueId
     *        The uniqueId of the time account whose details are wanted.
     * @return The details of the time account with the received UID.
     */
    public CDAAccountDetailOutput getTimeDetails(String accountUniqueId) {
        CDAAccountDetailOutput timeDetails = null;

        TimeAccountDetailsData[] timeAccountsDetails = getTimeAccountsDetails(accountUniqueId);
        if (timeAccountsDetails.length > 0) {
            timeDetails = timeAccountsDetails[0].getOutput();
        }

        return timeDetails;
    }

    /**
     * Retrieves the last update of the details of the time account with the received Unique ID.
     *
     * @param accountUniqueId
     *        The Unique Id of the time account whose details' last update is wanted.
     * @return The last update of the details of the time account with the received Unique ID.
     */
    public String getTimeDetailsLastUpdate(String accountUniqueId) {
        String lastUpdate = null;
        Cursor cursor = null;
        try {
            String whereClause = COL_UID + "=?";
            String[] whereArgs = new String[] {accountUniqueId};
            cursor = db.query(TABLE_NAME, null, whereClause, whereArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                lastUpdate = cursor.getString(cursor.getColumnIndex(COL_LAST_UPDATE));
            }
        } catch (Exception ex) {
            L.e(TAG, "getTimeDetailsLastUpdate error getting time account details last update "
                    + "accountUniqueId=" + accountUniqueId, ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return lastUpdate;
    }

    /**
     * Adds the received time account details to DB; if it already exists, updates it instead.
     *
     * @param timeDetails
     *        Object with the details of the time account
     * @param lastUpdateDate
     *        Date of the cache entry update
     * @param timeAccUID
     *        UniqueId of the time account whose details are being added / updated.
     *
     * @return True if the time account details were added / updated successfully; false otherwise.
     */
    public boolean addOrUpdateLocation(CDAAccountDetailOutput timeDetails, String lastUpdateDate, String timeAccUID) {
        boolean success = false;

        try {
            ContentValues values = new ContentValues();
            values.put(COL_ID, timeDetails.getId());

            CryptUtils.addEncryptedValue(COL_NAME, timeDetails.getName(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_RELATED_DDA_ACCOUNT_NUMBER, timeDetails.getRelatedDDAAccountNumber(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_OPENING_DATE, timeDetails.getOpeningDate(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_MATURITY, timeDetails.getMaturity(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_ACCOUNT_BALANCE, timeDetails.getAccountBalance(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_TERM, timeDetails.getTerm(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_TERM_UNIT, timeDetails.getTermUnit(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_INTEREST_RATE, timeDetails.getInterestRate(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_NEXT_INTEREST_PAYMENT, timeDetails.getNextInterestPayment(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_INTEREST_PAID_TO_RELATED_DDA, timeDetails.getInterestPaidToRelatedDDA(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_ROLLOVER_COUNT, timeDetails.getRolloverCount(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_GROSS_INTEREST, timeDetails.getGrossInterest(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_TAX_VALUE, timeDetails.getTaxValue(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_ACCOUNT_DESCRIPTION, timeDetails.getAccountDescription(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_AUTOMATIC_ROLLOVER, timeDetails.getAutomaticRollover(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_ACCOUNT_PLAN, timeDetails.getAccountPlan(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_BUSINESS_AREA, timeDetails.getBusinessArea(), encryptionSeed, values);
            CryptUtils.addEncryptedValue(COL_NET_INTEREST, timeDetails.getNetInterest(), encryptionSeed, values);

            values.put(COL_LAST_UPDATE, lastUpdateDate);

            String whereClause = COL_UID + "=?";
            String[] whereArgs = new String[] {timeAccUID};

            // Try to update the time account by its uniqueId
            success = db.update(TABLE_NAME, values, whereClause, whereArgs) > 0;

            L.d(TAG, "addOrUpdateTimeAccDetails update time acc details timeAccUID=" + timeAccUID + " ; success="
                    + success);

            // If update wasn't successful, try to insert new value
            if (!success) {
                values.put(COL_UID, timeAccUID);

                // Insert new value
                success = db.insert(TABLE_NAME, GENERIC_ID, values) != -1;

                L.d(TAG, "addOrUpdateTimeAccDetails inserted time acc details; timeAccUID=" + timeAccUID + " success="
                        + success);
            }

        } catch (Exception ex) {
            L.e(TAG, "addOrUpdateTimeAccDetails error adding time acc details lastUpdateDate=" + lastUpdateDate
                    + " timeAccUID=" + timeAccUID, ex);
        }

        return success;
    }

    /**
     * Deletes the details of the time account with the received Unique ID.
     *
     * @param accountUniqueId
     *        The Unique Id of the time account whose details are to be deleted.
     * @return True if the details of the time with the received Unique ID were deleted; false otherwise.
     */
    public boolean deleteTimeAccDetails(String accountUniqueId) {
        boolean success = false;

        try {
            String whereClause = COL_UID + "=?";
            String[] whereArgs = new String[] {accountUniqueId};
            success = db.delete(TABLE_NAME, whereClause, whereArgs) > 0;

            L.d(TAG, "deleteTimeAccDetails time account UID=" + accountUniqueId + " details deleted=" + success);
        } catch (Exception ex) {
            L.e(TAG, "deleteTimeAccDetails error deleting details of time account UID=" + accountUniqueId, ex);
        }

        return success;
    }

    // CacheHandler -----------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getTableNames() {
        return new String[] {TABLE_NAME};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getTablesCreationSQL() {
        return new String[] {CREATE_TIMEACC_DETAIS_TABLE_SQL};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteCache() {
        int deleted = -1;
        try {
            deleted = db.delete(TABLE_NAME, "1", null);
            L.d(TAG, "deleteCache deleted " + deleted + " elements");
        } catch (Exception e) {
            L.e(TAG, "deleteCache error deleting cache", e);
        }
        return deleted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void changeEncryption(String newEncryptionSeed) {
        // Get all data stored in the cache
        TimeAccountDetailsData[] allTimeAccountsDetails = getTimeAccountsDetails(null);

        // Delete cache
        deleteCache();
        // Change encryption seed
        this.encryptionSeed = newEncryptionSeed;

        // Reintroduce cache elements using new encryption seed
        for (TimeAccountDetailsData data : allTimeAccountsDetails) {
            addOrUpdateTimeAccDetails(data.getOutput(), data.getLastUpdateDate(), data.getAccountUID());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean copyTo(CacheHandler otherHandler, boolean move) {
        TimeAccountDetailsData[] allTimeAccountsDetails = getTimeAccountsDetails(null);

        AccountsTimeDetailsCacheHandler other = (AccountsTimeDetailsCacheHandler) otherHandler;
        int deletedElements = other.deleteCache();
        L.d(TAG, "copyTo move=" + move + " deletedElements=" + deletedElements);

        int added = 0;
        for (TimeAccountDetailsData data : allTimeAccountsDetails) {
            if (other.addOrUpdateTimeAccDetails(data.getOutput(), data.getLastUpdateDate(), data.getAccountUID())) {
                added++;
            }
        }

        if (move) {
            int deletedInThis = deleteCache();
            L.d(TAG, "copyTo moved; copied " + added + " of " + allTimeAccountsDetails.length
                    + " elements; deleted in this=" + deletedInThis);
        } else {
            L.d(TAG, "copyTo copied " + added + " of " + allTimeAccountsDetails.length + " elements");
        }

        return added == allTimeAccountsDetails.length;
    }
}
