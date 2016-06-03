package com.my.project;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Zip Utility
 */
public class ZipUtil {

    public static final Charset CHARSET_UTF8          = Charset.forName( "UTF-8" );
    public static final String  PATH_IN_ZIP_SEPARATOR = "/";
    private static final int    BUFFER_SIZE           = 4 << 10;

    /**
     * 将指定文件添加到Zip（UTF-8编码）
     * 
     * @param zipFilePath zip文件路径
     * @param filePaths 要添加的文件路径列表
     * @throws IOException
     */
    public static void compressFileToZip( String zipFilePath, String[] filePaths ) throws IOException {
        compressFileToZip( zipFilePath, "", CHARSET_UTF8, filePaths );
    }

    /**
     * 将指定文件添加到Zip（UTF-8编码）
     * 
     * @param zipFilePath zip文件路径
     * @param relativePathInZip zip文件内相对路径
     * @param filePaths 要添加的文件路径列表
     * @throws IOException
     */
    public static void compressFileToZip( String zipFilePath, String relativePathInZip, String[] filePaths )
            throws IOException {
        compressFileToZip( zipFilePath, relativePathInZip, CHARSET_UTF8, filePaths );
    }

    /**
     * 将指定文件添加到Zip
     * 
     * @param zipFilePath zip文件路径
     * @param relativePathInZip zip文件内相对路径
     * @param encoding 文件编码
     * @param filePaths 要添加的文件路径列表
     * @throws IOException
     */
    public static void compressFileToZip( String zipFilePath, String relativePathInZip, String encoding,
            String[] filePaths ) throws IOException {
        compressFileToZip( zipFilePath, relativePathInZip, Charset.forName( encoding ), filePaths );
    }

    /**
     * 将指定文件添加到Zip
     * 
     * @param zipFilePath zip文件路径
     * @param relativePathInZip zip文件内相对路径
     * @param encoding 文件编码
     * @param filePaths 要添加的文件路径列表
     * @throws IOException
     */
    public static void compressFileToZip( String zipFilePath, String relativePathInZip, Charset charset,
            String[] filePaths ) throws IOException {

        ZipOutputStream zipOutputStream = null;

        try {

            zipOutputStream = new ZipOutputStream( new FileOutputStream( zipFilePath ), charset );

            // 追加zip文件内相对路径
            String pathInZip = filePathToZipPath( relativePathInZip );
            if ( StringUtils.isNotBlank( pathInZip ) ) {
                ZipEntry zipEntry = new ZipEntry( pathInZip );
                zipOutputStream.putNextEntry( zipEntry );
                zipOutputStream.closeEntry();
            }

            // 读取要压缩的文件，添加到zip包中
            for ( String filePath : filePaths ) {
                File file = new File( filePath );
                if ( !file.exists() ) {
                    throw new FileNotFoundException( "要压缩的文件不存在：" + filePath );
                }
                // 将文件添加到zip包中
                compressFile( zipOutputStream, pathInZip, new File[] { file } );
            }
        } catch ( IOException e ) {
            throw e;
        } finally {
            if ( zipOutputStream != null ) {
                zipOutputStream.close();
            }
        }
    }

    /**
     * 将文件或目录添加到zip包中
     * 
     * @param zipOutputStream zip文件输出流
     * @param relativePathInZip zip文件内相对路径
     * @param files 多个文件或目录
     * @throws IOException
     */
    public static void compressFile( ZipOutputStream zipOutputStream, String relativePathInZip, File[] files )
            throws IOException {

        // 检查要压缩的文件
        relativePathInZip = filePathToZipPath( relativePathInZip );
        if ( ArrayUtils.isEmpty( files ) ) {
            throw new FileNotFoundException( "要压缩的文件不存在：" + files );
        }

        // 开始压缩文件
        for ( File file : files ) {
            if ( file.isDirectory() ) {

                // 添加目录到压缩包
                String newRelativePathInZip = relativePathInZip + file.getName() + PATH_IN_ZIP_SEPARATOR;
                ZipEntry zipEntry = new ZipEntry( newRelativePathInZip );
                zipOutputStream.putNextEntry( zipEntry );
                zipOutputStream.closeEntry();

                File[] filesInDirectory = file.listFiles();
                for ( int i = 0; i < filesInDirectory.length; i++ ) {
                    File tempFile = filesInDirectory[i];
                    if ( tempFile.isDirectory() ) {
                        // 目录
                        String newRelativePath = newRelativePathInZip + tempFile.getName() + PATH_IN_ZIP_SEPARATOR;

                        zipEntry = new ZipEntry( newRelativePath );
                        zipOutputStream.putNextEntry( zipEntry );
                        zipOutputStream.closeEntry();

                        compressFile( zipOutputStream, newRelativePath, tempFile );
                    } else {
                        // 文件
                        writeFileToZip( zipOutputStream, newRelativePathInZip, tempFile );
                    }
                }
            } else {
                // 文件
                writeFileToZip( zipOutputStream, relativePathInZip, file );
            }
        }
    }

    /**
     * 将文件或目录添加到zip包中
     * 
     * @param zipOutputStream zip文件输出流
     * @param relativePathInZip zip文件内相对路径
     * @param file 文件或目录
     * @throws IOException
     */
    public static void compressFile( ZipOutputStream zipOutputStream, String relativePathInZip, File file )
            throws IOException {

        // 检查要压缩的文件
        relativePathInZip = filePathToZipPath( relativePathInZip );
        if ( file == null || !file.exists() ) {
            throw new FileNotFoundException( "要压缩的文件不存在：" + file );
        }

        // 开始压缩文件
        if ( file.isDirectory() ) {
            File[] files = file.listFiles();
            for ( int i = 0; i < files.length; i++ ) {
                File tempFile = files[i];
                if ( tempFile.isDirectory() ) {
                    // 目录
                    String newRelativePath = relativePathInZip + tempFile.getName() + PATH_IN_ZIP_SEPARATOR;

                    ZipEntry zipEntry = new ZipEntry( newRelativePath );
                    zipOutputStream.putNextEntry( zipEntry );
                    zipOutputStream.closeEntry();

                    compressFile( zipOutputStream, newRelativePath, tempFile );
                } else {
                    // 文件
                    writeFileToZip( zipOutputStream, relativePathInZip, tempFile );
                }
            }
        } else {
            // 文件
            writeFileToZip( zipOutputStream, relativePathInZip, file );
        }
    }

    /**
     * 将文件添加到zip包中
     * 
     * @param zipOutputStream zip文件输出流
     * @param relativePathInZip zip文件内相对路径
     * @param file 文件
     * @throws IOException
     */
    private static void writeFileToZip( ZipOutputStream zipOutputStream, String relativePathInZip, File file )
            throws IOException {

        InputStream inputStream = null;

        try {
            ZipEntry entry = new ZipEntry( relativePathInZip + file.getName() );
            zipOutputStream.putNextEntry( entry );

            // 将文件写入zip包中
            inputStream = new FileInputStream( file );
            int length = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ( (length = inputStream.read( buffer, 0, BUFFER_SIZE )) >= 0 ) {
                zipOutputStream.write( buffer, 0, length );
            }

            zipOutputStream.flush();
            zipOutputStream.closeEntry();
        } catch ( IOException e ) {
            throw e;
        } finally {
            if ( inputStream != null ) {
                inputStream.close();
            }
        }
    }

    /**
     * 删除文件路径中的"\\"
     * 
     * @param filePath 文件路径
     * @return 处理后的文件路径
     */
    private static String filePathToZipPath( String filePath ) {

        if ( StringUtils.isBlank( filePath ) ) {
            return "";
        }

        String zipPath = new String( filePath );
        if ( zipPath.indexOf( "\\" ) != -1 ) {
            zipPath = zipPath.replaceAll( "\\", "/" );
        }
        if ( !zipPath.endsWith( "/" ) ) {
            zipPath = zipPath + "/";
        }

        return zipPath;
    }

    /**
     * 解压zip包
     * 
     * @param zipFilePath zip文件路径
     * @param targetPath 解压到的路径
     * @throws IOException
     */
    public static void decompressZipFile( String zipFilePath, String targetPath ) throws IOException {

        File file = new File( zipFilePath );
        if ( !file.exists() ) {
            throw new FileNotFoundException( "zip文件不存在：" + zipFilePath );
        }

        OutputStream outputStream = null;
        InputStream inputStream = null;
        ZipFile zipFile = null;

        try {
            zipFile = new ZipFile( zipFilePath );

            // 创建文件夹
            String directoryPath = "";
            if ( StringUtils.isBlank( targetPath ) ) {
                directoryPath = zipFilePath.substring( 0, zipFilePath.lastIndexOf( "." ) ) + File.separator;
            } else {
                directoryPath = targetPath.endsWith( File.separator ) ? targetPath : targetPath + File.separator;
            }
            FileUtils.forceMkdir( new File( directoryPath ) );

            // 开始解压zip内的文件
            Enumeration < ? > entryEnum = zipFile.entries();
            if ( entryEnum != null ) {
                ZipEntry zipEntry = null;
                while ( entryEnum.hasMoreElements() ) {
                    zipEntry = (ZipEntry) entryEnum.nextElement();
                    if ( zipEntry.isDirectory() ) {
                        // 解压目录
                        FileUtils.forceMkdir( new File( directoryPath + zipEntry.getName() ) );
                        continue;
                    } else {
                        // 解压文件
                        File targetFile = new File( directoryPath + zipEntry.getName() );
                        FileUtils.forceMkdir( targetFile.getParentFile() );
                        if ( !targetFile.exists() ) {
                            targetFile.createNewFile();
                        }
                        outputStream = new BufferedOutputStream( new FileOutputStream( targetFile ) );
                        inputStream = zipFile.getInputStream( zipEntry );
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int length = 0;
                        while ( (length = inputStream.read( buffer, 0, BUFFER_SIZE )) >= 0 ) {
                            outputStream.write( buffer, 0, length );
                        }
                        outputStream.flush();
                        outputStream.close();
                    }
                }
            }
        } catch ( IOException e ) {
            throw e;
        } finally {
            if ( outputStream != null ) {
                outputStream.close();
            }
            if ( inputStream != null ) {
                inputStream.close();
            }
            if ( zipFile != null ) {
                zipFile.close();
            }
        }
    }

    /**
     * 将文件转为base64字符串
     * 
     * @param filePath 文件路径
     * @return base64字符串
     * @throws IOException
     */
    public static String fileToBase64( String filePath ) throws IOException {

        File file = new File( filePath );
        if ( !file.exists() ) {
            throw new FileNotFoundException( "要转换的文件不存在" + filePath );
        }

        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        byte[] data = null;
        try {
            inputStream = new FileInputStream( file );
            outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[BUFFER_SIZE];
            int length = 0;
            while ( (length = inputStream.read( buffer, 0, BUFFER_SIZE )) >= 0 ) {
                outputStream.write( buffer, 0, length );
            }
            data = outputStream.toByteArray();
        } catch ( IOException e ) {
            throw e;
        } finally {
            if ( inputStream != null ) {
                inputStream.close();
            }
            if ( outputStream != null ) {
                outputStream.close();
            }
        }

        return Base64.encodeBase64String( data );
    }

    /**
     * 将base64字符串转为文件
     * 
     * @param base64String base64字符串
     * @param outputFilePath 文件路径
     * @throws IOException
     */
    public static void base64ToFile( String base64String, String outputFilePath ) throws IOException {

        byte[] data = Base64.decodeBase64( base64String );

        OutputStream out = null;
        try {
            out = new FileOutputStream( outputFilePath );
            out.write( data );
            out.flush();
        } catch ( IOException e ) {
            throw e;
        } finally {
            if ( out != null ) {
                out.close();
            }
        }
    }
}
