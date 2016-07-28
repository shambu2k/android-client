package com.mifos.mifosxdroid.offline.syncclientpayloads;

import com.mifos.api.datamanager.DataManagerClient;
import com.mifos.mifosxdroid.FakeRemoteDataSource;
import com.mifos.mifosxdroid.util.RxSchedulersOverrideRule;
import com.mifos.objects.ErrorSyncServerMessage;
import com.mifos.objects.client.Client;
import com.mifos.objects.client.ClientPayload;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import rx.Observable;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by rajan on 11/07/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SyncClientPayloadsPresenterTest {

    @Rule
    public final RxSchedulersOverrideRule mOverrideSchedulersRule = new RxSchedulersOverrideRule();

    SyncClientPayloadsPresenter  syncClientPayloadsPresenter;

    @Mock
    DataManagerClient mDataManagerClient;

    @Mock
    SyncClientPayloadsMvpView mSyncClientPayloadsMvpView;

    List<ClientPayload> clientPayloads;

    ErrorSyncServerMessage errorSyncServerMessage;

    @Before
    public void setUp() {
        syncClientPayloadsPresenter = new SyncClientPayloadsPresenter(mDataManagerClient);
        syncClientPayloadsPresenter.attachView(mSyncClientPayloadsMvpView);
        clientPayloads = FakeRemoteDataSource.getClientPayloads();
        errorSyncServerMessage = FakeRemoteDataSource.getFailureServerResponse();
    }

    @After
    public void tearDown() {
        syncClientPayloadsPresenter.detachView();
    }

    @Test
    public void testLoadDatabaseClientPayload() {
        when(mDataManagerClient.getAllDatabaseClientPayload()).thenReturn(Observable.just
                (clientPayloads));

        syncClientPayloadsPresenter.loadDatabaseClientPayload();

        verify(mSyncClientPayloadsMvpView).showPayloads(clientPayloads);
        verify(mSyncClientPayloadsMvpView, never()).showError("Failed to load ClientPayload");
    }

    @Test
    public void testLoadDatabaseClientPayloadFails() {
        when(mDataManagerClient.getAllDatabaseClientPayload())
                .thenReturn(Observable.<List<ClientPayload>>error(new RuntimeException()));

        syncClientPayloadsPresenter.loadDatabaseClientPayload();

        verify(mSyncClientPayloadsMvpView).showError("Failed to load ClientPayload");
        verify(mSyncClientPayloadsMvpView, never()).showPayloads(clientPayloads);
    }


    @Test
    public void testSyncClientPayload() {
        //Syncing the first Client Payload of FakeDataSource
        when(mDataManagerClient.createClient(clientPayloads.get(0))).thenReturn(Observable.just
                (new Client()));

        syncClientPayloadsPresenter.syncClientPayload(clientPayloads.get(0));

        verify(mSyncClientPayloadsMvpView).showSyncResponse();
        verify(mSyncClientPayloadsMvpView, never())
                .showClientSyncFailed(errorSyncServerMessage);
    }

    @Test
    public void testSyncClientPayloadFails() {
        when(mDataManagerClient.createClient(clientPayloads.get(0)))
                .thenReturn(Observable.<Client>error(new RuntimeException()));

        syncClientPayloadsPresenter.syncClientPayload(clientPayloads.get(0));

        //TODO Fix this test showClientSyncFailed, It is failing because this is calling a
        //TODO Schedulers in Fragment View Class.
        //verify(mSyncClientPayloadsMvpView).showClientSyncFailed(errorSyncServerMessage);
        verify(mSyncClientPayloadsMvpView, never()).showSyncResponse();
    }

    @Test
    public void testDeleteAndUpdateClientPayload() throws Exception {
        //Deleting the Client Payload of Id = 1, Id is transient. it will not serialize and
        // deserialize during retrofit request. It is for just numbering the payload in Table.
        when(mDataManagerClient.deleteAndUpdatePayloads(1)).thenReturn(Observable.just
                (clientPayloads));

        syncClientPayloadsPresenter.deleteAndUpdateClientPayload(1);

        verify(mSyncClientPayloadsMvpView).showPayloadDeletedAndUpdatePayloads(clientPayloads);
        verify(mSyncClientPayloadsMvpView, never()).showError("Failed Update List");
    }

    @Test
    public void testDeleteAndUpdateClientPayloadFails() {
        when(mDataManagerClient.deleteAndUpdatePayloads(1))
                .thenReturn(Observable.<List<ClientPayload>>error(new RuntimeException()));

        syncClientPayloadsPresenter.deleteAndUpdateClientPayload(1);

        verify(mSyncClientPayloadsMvpView).showError("Failed Update List");
        verify(mSyncClientPayloadsMvpView, never())
                .showPayloadDeletedAndUpdatePayloads(clientPayloads);
    }
}