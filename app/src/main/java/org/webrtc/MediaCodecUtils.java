//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;

class MediaCodecUtils {
    private static final String TAG = "MediaCodecUtils";
    static final String EXYNOS_PREFIX = "OMX.Exynos.";
    static final String INTEL_PREFIX = "OMX.Intel.";
    static final String NVIDIA_PREFIX = "OMX.Nvidia.";
    static final String QCOM_PREFIX = "OMX.qcom.";
    static final int COLOR_QCOM_FORMATYVU420PackedSemiPlanar32m4ka = 2141391873;
    static final int COLOR_QCOM_FORMATYVU420PackedSemiPlanar16m4ka = 2141391874;
    static final int COLOR_QCOM_FORMATYVU420PackedSemiPlanar64x32Tile2m8ka = 2141391875;
    static final int COLOR_QCOM_FORMATYUV420PackedSemiPlanar32m = 2141391876;
    static final int[] DECODER_COLOR_FORMATS = new int[]{19, 21, 2141391872, 2141391873, 2141391874, 2141391875, 2141391876};
    static final int[] ENCODER_COLOR_FORMATS = new int[]{19, 21, 2141391872, 2141391876};

    static Integer selectColorFormat(int[] supportedColorFormats, CodecCapabilities capabilities) {
        int[] var2 = supportedColorFormats;
        int var3 = supportedColorFormats.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            int supportedColorFormat = var2[var4];
            int[] var6 = capabilities.colorFormats;
            int var7 = var6.length;

            for(int var8 = 0; var8 < var7; ++var8) {
                int codecColorFormat = var6[var8];
                if (codecColorFormat == supportedColorFormat) {
                    return codecColorFormat;
                }
            }
        }

        return null;
    }

    static boolean codecSupportsType(MediaCodecInfo info, VideoCodecType type) {
        String[] var2 = info.getSupportedTypes();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String mimeType = var2[var4];
            if (type.mimeType().equals(mimeType)) {
                return true;
            }
        }

        return false;
    }

    private MediaCodecUtils() {
    }
}
