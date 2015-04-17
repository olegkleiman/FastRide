package com.maximum.fastride.utils;

import com.google.gson.JsonObject;
import com.microsoft.windowsazure.mobileservices.table.MobileServicePreconditionFailedException;
import com.microsoft.windowsazure.mobileservices.table.sync.operations.RemoteTableOperationProcessor;
import com.microsoft.windowsazure.mobileservices.table.sync.operations.TableOperation;
import com.microsoft.windowsazure.mobileservices.table.sync.push.MobileServicePushCompletionResult;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.MobileServiceSyncHandler;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.MobileServiceSyncHandlerException;

/**
 * Created by Oleg Kleiman on 17-Apr-15.
 */
public class ConflictResolvingSyncHandler implements MobileServiceSyncHandler {
    @Override
    public JsonObject executeTableOperation(RemoteTableOperationProcessor remoteTableOperationProcessor,
                                            TableOperation tableOperation)
            throws MobileServiceSyncHandlerException {
        MobileServicePreconditionFailedException ex = null;
        JsonObject result = null;
        try {
            result = tableOperation.accept(remoteTableOperationProcessor);
        } catch (MobileServicePreconditionFailedException e) {
            ex = e;
        } catch (Throwable e) {
            ex = (MobileServicePreconditionFailedException) e.getCause();
        }

        if (ex != null) {
            // A conflict was detected; let's force the server to "win"
            // by discarding the client version of the item
            // Other policies could be used, such as prompt the user for
            // which version to maintain.
            JsonObject serverItem = (JsonObject)ex.getItem();
            if (serverItem == null) {
                // Item not returned in the exception, retrieving it from the server
//                try {
//                    serverItem = wamsClient.getTable(operation.getTableName()).lookUp(operation.getItemId()).get();
//                } catch (Exception e) {
//                    throw new MobileServiceSyncHandlerException(e);
//                }
            }

            result = serverItem;
        }

        return result;
    }

    @Override
    public void onPushComplete(MobileServicePushCompletionResult mobileServicePushCompletionResult) throws MobileServiceSyncHandlerException {

    }
}
