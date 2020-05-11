//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.webrtc;

import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.os.Build.VERSION;

public class HardwareVideoDecoderFactory implements VideoDecoderFactory {
    private static final String TAG = "HardwareVideoDecoderFactory";

    public HardwareVideoDecoderFactory() {
    }

    public VideoDecoder createDecoder(String codecType) {
        VideoCodecType type = VideoCodecType.valueOf(codecType);
        MediaCodecInfo info = this.findCodecForType(type);
        if (info == null) {
            return null;
        } else {
            CodecCapabilities capabilities = info.getCapabilitiesForType(type.mimeType());
            return new HardwareVideoDecoder(info.getName(), type, MediaCodecUtils.selectColorFormat(MediaCodecUtils.DECODER_COLOR_FORMATS, capabilities));
        }
    }

    private MediaCodecInfo findCodecForType(VideoCodecType type) {
        if (VERSION.SDK_INT < 19) {
            return null;
        } else {
            for(int i = 0; i < MediaCodecList.getCodecCount(); ++i) {
                MediaCodecInfo info = null;

                try {
                    info = MediaCodecList.getCodecInfoAt(i);
                } catch (IllegalArgumentException var5) {
                    Logging.e("HardwareVideoDecoderFactory", "Cannot retrieve encoder codec info", var5);
                }

                if (info != null && !info.isEncoder() && this.isSupportedCodec(info, type)) {
                    return info;
                }
            }

            return null;
        }
    }

    private boolean isSupportedCodec(MediaCodecInfo info, VideoCodecType type) {
        if (!MediaCodecUtils.codecSupportsType(info, type)) {
            return false;
        } else {
            return MediaCodecUtils.selectColorFormat(MediaCodecUtils.DECODER_COLOR_FORMATS, info.getCapabilitiesForType(type.mimeType())) == null ? false : this.isHardwareSupported(info, type);
        }
    }

    private boolean isHardwareSupported(MediaCodecInfo info, VideoCodecType type) {
        String name = info.getName();
        switch(type) {
            case VP8:
                return name.startsWith("OMX.qcom.") || name.startsWith("OMX.Intel.") || name.startsWith("OMX.Exynos.") || name.startsWith("OMX.Nvidia.");
            case VP9:
                return name.startsWith("OMX.qcom.") || name.startsWith("OMX.Exynos.");
            case H264:
                return name.startsWith("OMX.qcom.") || name.startsWith("OMX.Intel.") || name.startsWith("OMX.Exynos.");
            default:
                return false;
        }
    }
}
