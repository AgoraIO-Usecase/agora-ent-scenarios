package io.agora.lrcview;

import androidx.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import io.agora.lrcview.bean.LrcData;

/**
 * 加载歌词
 *
 * @author chenhengfei(Aslanchen)
 * @date 2021/7/6
 */
public class LrcLoadUtils {

    @Nullable
    public static LrcData parse(File lrcFile) {
        return parse(null, lrcFile);
    }

    @Nullable
    public static LrcData parse(LrcData.Type type, File lrcFile) {
        if (type == null) {
            InputStream instream = null;
            InputStreamReader inputreader = null;
            BufferedReader buffreader = null;
            try {
                instream = new FileInputStream(lrcFile);
                inputreader = new InputStreamReader(instream);
                buffreader = new BufferedReader(inputreader);
                String line = buffreader.readLine();
                if (line.contains("xml")) {
                    type = LrcData.Type.Migu;
                } else {
                    type = LrcData.Type.Default;
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (instream != null) {
                        instream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (inputreader != null) {
                        inputreader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (buffreader != null) {
                        buffreader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (type == LrcData.Type.Default) {
            return LrcLoadDefaultUtils.parseLrc(lrcFile);
        } else if (type == LrcData.Type.Migu) {
            return LrcLoadMiguUtils.parseLrc(lrcFile);
        } else {
            return null;
        }
    }
}
