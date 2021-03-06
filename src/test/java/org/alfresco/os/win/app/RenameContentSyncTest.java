/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 * This file is part of Alfresco
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.os.win.app;

import java.io.File;

import org.alfresco.po.share.site.document.DocumentLibraryPage;
import org.alfresco.sync.DesktopSyncTest;
import org.alfresco.test.AlfrescoTest;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.TestException;
import org.testng.annotations.Test;

/**
 * This class will contain all the test cases related to Rename of file in both
 * client (Windows machine) and share
 * 
 * @author Sprasanna
 * @author Paul Brodner
 */
public class RenameContentSyncTest extends DesktopSyncTest
{
    private static final Logger logger = Logger.getLogger(RenameContentSyncTest.class);
    Notepad notepad = new Notepad();
    WindowsExplorer explorer = new WindowsExplorer();

    /**
     * Rename of file in client
     * Step1 - create a file in client sync location
     * Step2 - Wait for the sync time -Client sync time
     * Step3 - Validate in share whether the file is available correctly
     * Step4 - In client rename the file using right click rename and enter the new name
     * Step5 - wait for the sync time - Client sync time
     * Step6 - Validate the file name is
     * changed in share
     */
    @AlfrescoTest(testlink = "ALF-2613")
    @Test
    public void renameFileInClient()
    {
        File fileToRename = getRandomFileIn(getLocalSiteLocation(), "fileToRename", "txt");
        File fileRenamed = getRandomFileIn(getLocalSiteLocation(), "renamedFile", "txt");
        try
        {
            notepad.openApplication();
            notepad.saveAs(fileToRename);
            notepad.close(fileToRename);
            syncWaitTime(CLIENTSYNCTIME);
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, fileToRename.getName()), "File was synced successfuly to Share");

            explorer.openApplication();
            explorer.openFolder(fileToRename.getParentFile());
            explorer.rename(fileToRename, fileRenamed);
            explorer.closeExplorer();

            syncWaitTime(CLIENTSYNCTIME);
            share.refreshSharePage(drone);
            Assert.assertTrue(share.isFileVisible(drone, fileRenamed.getName()));
            Assert.assertFalse(share.isFileVisible(drone, fileToRename.getName()));
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new TestException("Test case failed - renameFileInClient ", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    /**
     * rename a folder with file in client
     * Step1 - open the sync location
     * Step2 - Create a new folder (folderclientrename)
     * Step3 - Navigate to the newly created folder and upload a file (fileclientrename)
     * Step4 - Navigate back to the synclocation
     * Step5 - Wait for the sync time - Client sync time
     * Step6 - Check in share whether the folder and file is visible
     * Step7 - In client rename the folder (renameclientfolder)
     * Step8 - Wait for the sync time - client sync time
     * Step9 - validate in share whether the folder is renamed
     * Step10 - Validate whether the file is still present inside the folder.
     */
    @Test
    public void renameFolderInClient()
    {
        File folder = getRandomFolderIn(getLocalSiteLocation(), "rnmFolder");
        File folderRename = getRandomFolderIn(getLocalSiteLocation(), "renamedFolder");

        File folderFile = getRandomFileIn(folder, "fileR", "txt");
        try
        {
            explorer.openApplication();
            explorer.openFolder(folder.getParentFile());
            explorer.createAndOpenFolder(folder.getName());
            explorer.goBack(getLocalSiteLocation().getName());
            notepad.openApplication();
            notepad.saveAs(folderFile);
            notepad.close(folderFile);
            syncWaitTime(CLIENTSYNCTIME);
            
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue(share.isFileVisible(drone, folder.getName()), "Folder was synced in Share");
            share.navigateToFolder(drone, folder.getName());
            Assert.assertTrue(share.isFileVisible(drone, folderFile.getName()), "File created in Folder was synced in Share");
            explorer.rename(folder, folderRename);
            syncWaitTime(CLIENTSYNCTIME);
            
            DocumentLibraryPage docLib = (DocumentLibraryPage) share.refreshSharePage(drone);
            docLib.selectDocumentLibrary(drone);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            Assert.assertTrue((share.isFileVisible(drone, folderRename.getName())), "Renamed folder was synced in Share");
            Assert.assertFalse(share.isFileVisible(drone,folderFile.getName()), "old folder is renamed correctly");
            share.navigateToFolder(drone, folderRename.getName());
            Assert.assertTrue((share.isFileVisible(drone, folderFile.getName())), "File exists in renamed folder in Share");
        }
        catch (Throwable e)
        {
            logger.error(e);
            throw new TestException("Test case failed- renameFolderInClient", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    /**
     * Rename of file in share
     * Step1 - Login into share
     * Step2 - Upload the file in site document library
     * Step3 - Wait for the sync time - Server sync time
     * Step4 - Check in the client sync location the file is visible.
     * Step6- In share rename the file
     * Step7 - Wait for the sync time - server sync time
     * Step8 - In client sync location the file is renamed correctly
     */
    @AlfrescoTest(testlink = "ALF-2616")
    @Test
    public void renameFileInShare()
    {
        File fileTest = getRandomFileIn(getLocalSiteLocation(), "renameFileSh", "txt");
        File renamedFile = getRandomFileIn(getLocalSiteLocation(), "renamedFileSh", "txt");
        try
        {
            File fileInShare = share.newFile(fileTest.getPath(), fileTest.getName());
            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(fileInShare.exists(), "File exists in Client");
            
            share.editContentNameInline(drone, fileTest.getName(), renamedFile.getName(), true);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(fileTest.exists(), "Original file does not exists in Client");
            Assert.assertTrue(renamedFile.exists(), "Renamed file in Share is now synced in Client");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new TestException("Test case failed-renameFileInShare", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }

    // /**
    // * Rename of empty folder in share
    // * Step1 - login in to share
    // * Step2 - Create a folder in the document library
    // * Step3 - Wait for the sync time - server sync time
    // * Step4 - Rename the folder in share
    // * Step5 - Wait for the sync time - server sync time
    // * Step6 - Validate in client the folder name is
    // * changed to the renamed name.
    // */
    // // @Test
    // public void renameFolderInShare()
    // {
    // File folderInShare = getRandomFolderIn(getLocalSiteLocation(), "fldInShare");
    // File folderInShareRenamed = getRandomFolderIn(getLocalSiteLocation(), "renamedFldInShare");
    // try
    // {
    // shareLogin.loginToShare(drone, userInfo, shareUrl);
    // share.openSitesDocumentLibrary(drone, siteName);
    // share.navigateToFolder(drone, getLocalSiteLocation().getName());
    // share.createFolder(drone, folderInShare.getName(), folderInShare.getName(), folderInShare.getName());
    // syncWaitTime(SERVERSYNCTIME);
    // Assert.assertTrue(folderInShare.exists(), "Folder created in Share was synched to Client");
    // share.editContentNameInline(drone, folderInShare.getName(), folderInShareRenamed.getName(), true);
    // syncWaitTime(SERVERSYNCTIME);
    // Assert.assertFalse(folderInShare.exists(), "Original folder does not exists in Client");
    // Assert.assertTrue(folderInShareRenamed.exists(), "Renamed folder exists in Client");
    // }
    // catch (Throwable e)
    // {
    // e.printStackTrace();
    // throw new TestException("test case failed-renameFolderInShare", e);
    // }
    // finally
    // {
    // shareLogin.logout(drone);
    // }
    // }

    /**
     * Rename a folder with File in share
     */
    @Test
    public void renameFolderWithFileInShare()
    {
        File folderInShare = getRandomFolderIn(getLocalSiteLocation(), "fldInShare");
        File folderInShareRenamed = getRandomFolderIn(getLocalSiteLocation(), "renamedFldInShare");
        File fileTest = getRandomFileIn(folderInShareRenamed, "file", "txt");
        try
        {
            File fileInShare = share.newFile(fileTest.getName(), fileTest.getName());

            shareLogin.loginToShare(drone, userInfo, shareUrl);
            share.openSitesDocumentLibrary(drone, siteName);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            share.createFolder(drone, folderInShare.getName(), folderInShare.getName(), folderInShare.getName());
            share.navigateToFolder(drone, folderInShare.getName());
            share.uploadFile(drone, fileInShare);
            
            // Goto back to the root folder 
            DocumentLibraryPage docLib = (DocumentLibraryPage) share.refreshSharePage(drone);
            docLib.selectDocumentLibrary(drone);
            share.navigateToFolder(drone, getLocalSiteLocation().getName());
            fileInShare.delete(); 

            syncWaitTime(SERVERSYNCTIME);
            Assert.assertTrue(folderInShare.exists(), "Folder created in Share was synched to Client");
            share.editContentNameInline(drone, folderInShare.getName(), folderInShareRenamed.getName(), true);
            syncWaitTime(SERVERSYNCTIME);
            Assert.assertFalse(folderInShare.exists(), "Original folder does not exists in Client");
            Assert.assertTrue(folderInShareRenamed.exists(), "Renamed folder exists in Client");
            Assert.assertTrue(fileTest.exists(), "File from renamed folder exists in Client");
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            throw new TestException("Test case failed-renameFolderInShare", e);
        }
        finally
        {
            shareLogin.logout(drone);
        }
    }
}
