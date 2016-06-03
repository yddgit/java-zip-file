package com.my.project;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * ZipUtil测试类
 * 
 * <pre>
 * 测试文件目录结构：
 * ├─zip-util-test-original
 * │  ├─file1.txt
 * │  ├─file2.txt
 * │  └─dir1
 * │     ├─fileindir1.txt
 * │     └─fileindir2.txt
 * └─zip-util-test-new
 * </pre>
 * @author yang.dongdong
 *
 */
public class ZipUtilTest {

    /** /zip-util-test-original/ */
    private static final String TEST_DIR = System.getProperty( "java.io.tmpdir" ) + File.separator + "zip-util-test-original";
    /** /zip-util-test-original/dir1/ */
    private static final String DIR_IN_TEST_DIR = TEST_DIR + File.separator + "dir1";
    /** /zip-util-test-original/dir1/fileindir1.txt */
    private static final String FILE_IN_DIR_1 = TEST_DIR + File.separator + "dir1" + File.separator + "fileindir1.txt";
    /** /zip-util-test-original/dir1/fileindir2.txt */
    private static final String FILE_IN_DIR_2 = TEST_DIR + File.separator + "dir1" + File.separator + "fileindir2.txt";
    /** /zip-util-test-original/file1.txt */
    private static final String FILE_1 = TEST_DIR + File.separator + "file1.txt";
    /** /zip-util-test-original/file2.txt */
    private static final String FILE_2 = TEST_DIR + File.separator + "file2.txt";
    /** /zip-util-test-new/ */
    private static final String TARGET_DIR = System.getProperty( "java.io.tmpdir" ) + File.separator + "zip-util-test-new";

    @Before
    public void before() throws IOException {
        if(!new File(TEST_DIR).exists()) {
            FileUtils.forceMkdir(new File(TEST_DIR));
            FileUtils.forceMkdir(new File(DIR_IN_TEST_DIR));
            FileUtils.write(new File(FILE_IN_DIR_1), "fileindir1", "UTF-8", false);
            FileUtils.write(new File(FILE_IN_DIR_2), "fileindir2", "UTF-8", false);
            FileUtils.write(new File(FILE_1), "file1", "UTF-8", false);
            FileUtils.write(new File(FILE_2), "file2", "UTF-8", false);
        }
        if(new File(TARGET_DIR).exists()) {
            FileUtils.deleteQuietly(new File(TARGET_DIR));
        }
        FileUtils.forceMkdir(new File(TARGET_DIR));
    }

    @Test
    public void compressFile() throws IOException {
        ZipUtil.compressFileToZip( TARGET_DIR + File.separator + "zip-file.zip" , new String[] { FILE_1, FILE_2 } );
    }
    
    @Test
    public void compressDirectory() throws IOException {
        ZipUtil.compressFileToZip( TARGET_DIR + File.separator + "zip-file.zip" , new String[] { DIR_IN_TEST_DIR } );
    }

    @Test
    public void compressFiles() throws IOException {
        ZipUtil.compressFileToZip( TARGET_DIR + File.separator + "zip-file.zip" , new String[] { DIR_IN_TEST_DIR, FILE_1 } );
    }

    @Test
    public void decompressFile() throws IOException {
        ZipUtil.compressFileToZip( TARGET_DIR + File.separator + "zip-file.zip" , new String[] { DIR_IN_TEST_DIR, FILE_1 } );
        ZipUtil.decompressZipFile( TARGET_DIR + File.separator + "zip-file.zip", TARGET_DIR + File.separator + "zip-file1" );
        ZipUtil.decompressZipFile( TARGET_DIR + File.separator + "zip-file.zip", null );
    }

    @Test
    public void fileToBase64() throws IOException {
        ZipUtil.compressFileToZip( TARGET_DIR + File.separator + "zip-file.zip" , new String[] { DIR_IN_TEST_DIR, FILE_1 } );
        String zipFileBase64String = ZipUtil.fileToBase64( TARGET_DIR + File.separator + "zip-file.zip" );
        System.out.println(zipFileBase64String);
    }

    @Test
    public void base64ToFile() throws IOException {
        ZipUtil.compressFileToZip( TARGET_DIR + File.separator + "zip-file.zip" , new String[] { DIR_IN_TEST_DIR, FILE_1 } );
        String zipFileBase64String = ZipUtil.fileToBase64( TARGET_DIR + File.separator + "zip-file.zip" );
        ZipUtil.base64ToFile( zipFileBase64String, TARGET_DIR + File.separator + "zip-file1.zip" );
    }
}
