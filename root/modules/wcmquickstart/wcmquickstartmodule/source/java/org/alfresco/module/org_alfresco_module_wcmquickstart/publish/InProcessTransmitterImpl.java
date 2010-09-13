/*
 * Copyright (C) 2009-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_wcmquickstart.publish;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transfer.Transfer;
import org.alfresco.repo.transfer.TransferCommons;
import org.alfresco.repo.transfer.TransferTransmitter;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.transfer.TransferException;
import org.alfresco.service.cmr.transfer.TransferProgress;
import org.alfresco.service.cmr.transfer.TransferReceiver;
import org.alfresco.service.cmr.transfer.TransferTarget;
import org.alfresco.service.transaction.TransactionService;

/**
 * This class delegates transfer service to the transfer receiver without using
 * any networking.
 * 
 * It is used for unit testing the transfer service without requiring two
 * instance of the repository to be running.
 * 
 * @author Mark Rogers
 */
public class InProcessTransmitterImpl implements TransferTransmitter
{
    private TransferReceiver receiver;
    private ContentService contentService;
    private TransactionService transactionService;

    public InProcessTransmitterImpl()
    {
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public Transfer begin(final TransferTarget target) throws TransferException
    {
        return transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Transfer>()
                {
                    public Transfer execute() throws Throwable
                    {
                        Transfer transfer = new Transfer();
                        String transferId = receiver.start();
                        transfer.setTransferId(transferId);
                        transfer.setTransferTarget(target);
                        return transfer;
                    }
                }, false, true);
    }

    public void abort(final Transfer transfer) throws TransferException
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Transfer>()
                {
                    public Transfer execute() throws Throwable
                    {
                        String transferId = transfer.getTransferId();
                        receiver.cancel(transferId);
                        return null;
                    }
                }, false, true);
    }

    public void commit(final Transfer transfer) throws TransferException
    {
        transactionService.getRetryingTransactionHelper().doInTransaction(
                new RetryingTransactionHelper.RetryingTransactionCallback<Transfer>()
                {
                    public Transfer execute() throws Throwable
                    {
                        String transferId = transfer.getTransferId();
                        receiver.commit(transferId);
                        return null;
                    }
                }, false, true);
    }

    public void prepare(Transfer transfer) throws TransferException
    {
        String transferId = transfer.getTransferId();
        receiver.prepare(transferId);
    }

    public void sendContent(Transfer transfer, Set<ContentData> data)
    {
        String transferId = transfer.getTransferId();

        for (ContentData content : data)
        {
            String contentUrl = content.getContentUrl();
            String fileName = TransferCommons.URLToPartName(contentUrl);

            InputStream contentStream = getContentService().getRawReader(contentUrl).getContentInputStream();
            receiver.saveContent(transferId, fileName, contentStream);
        }
    }

    public void sendManifest(Transfer transfer, File manifest, OutputStream results) throws TransferException
    {
        try
        {
            String transferId = transfer.getTransferId();
            FileInputStream fs = new FileInputStream(manifest);
            receiver.saveSnapshot(transferId, fs);
            receiver.generateRequsite(transferId, results);
            results.close();
        } 
        catch (FileNotFoundException error)
        {
            throw new TransferException("Failed to find snapshot file: " + manifest.getPath(), error);
        }
        catch (IOException e)
        {
            throw new TransferException("Failed to either read snapshot file or write requisite file: " + 
                    manifest.getPath(), e);
        }
    }

    public void verifyTarget(TransferTarget target) throws TransferException
    {

    }

    public TransferProgress getStatus(Transfer transfer) throws TransferException
    {
        String transferId = transfer.getTransferId();
        return receiver.getStatus(transferId);
    }

    public void setReceiver(TransferReceiver receiver)
    {
        this.receiver = receiver;
    }

    private ContentService getContentService()
    {
        return contentService;
    }

	@Override
	public void getTransferReport(Transfer transfer, OutputStream results) 
	{
		
	}

}
