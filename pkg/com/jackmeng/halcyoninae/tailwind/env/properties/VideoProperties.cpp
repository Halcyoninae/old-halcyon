#include "VideoProperties.hpp"

#include <AvTranscoder/util.hpp>
#include <AvTranscoder/decoder/VideoDecoder.hpp>
#include <AvTranscoder/data/decoded/VideoFrame.hpp>
#include <AvTranscoder/properties/util.hpp>
#include <AvTranscoder/properties/FileProperties.hpp>
#include <AvTranscoder/progress/NoDisplayProgress.hpp>

extern "C" {
#include <libavutil/avutil.h>
}

#include <stdexcept>
#include <iomanip>
#include <sstream>
#include <limits>
#include <cmath>

namespace avtranscoder
{

VideoProperties::VideoProperties(const FileProperties& fileProperties, const size_t index, IProgress& progress,
                                 const EAnalyseLevel level)
    : StreamProperties(fileProperties, index)
    , _levelAnalysis(level)
    , _pixelProperties()
    , _isInterlaced(false)
    , _isTopFieldFirst(false)
    , _gopSize(0)
    , _gopStructure()
    , _nbFrames(0)
    , _firstGopTimeCode(-1)
{
    if(_codecContext)
    {
        _pixelProperties = PixelProperties(_codecContext->pix_fmt);
        _firstGopTimeCode = _formatContext->start_time;
    }

    switch(_levelAnalysis)
    {
        case eAnalyseLevelFirstGop:
            analyseGopStructure(progress);
            break;
        case eAnalyseLevelFull:
            analyseFull(progress);
            break;
        default:
            break;
    }

    if(_levelAnalysis > eAnalyseLevelHeader)
    {
        // Returns at the beginning of the stream
        const_cast<InputFile&>(_fileProperties->getInputFile()).seekAtFrame(0, AVSEEK_FLAG_BYTE);
    }
}

std::string VideoProperties::getProfileName() const
{
    if(!_codecContext || !_codec)
        throw std::runtime_error("unknown codec");

#ifdef AV_CODEC_CAP_TRUNCATED
    if(_codec->capabilities & AV_CODEC_CAP_TRUNCATED)
        _codecContext->flags |= AV_CODEC_FLAG_TRUNCATED;
#else
    if(_codec->capabilities & CODEC_CAP_TRUNCATED)
        _codecContext->flags |= CODEC_FLAG_TRUNCATED;
#endif

    const char* profile = NULL;
    if((profile = av_get_profile_name(_codec, getProfile())) == NULL)
        throw std::runtime_error("unknown codec profile");

    return std::string(profile);
}

std::string VideoProperties::getColorTransfert() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");

    switch(_codecContext->color_trc)
    {
        case AVCOL_TRC_BT709:
            return "Rec 709 / ITU-R BT1361";
        case AVCOL_TRC_UNSPECIFIED:
            return "unspecified";
        case AVCOL_TRC_GAMMA22:
            return "Gamma 2.2";
        case AVCOL_TRC_GAMMA28:
            return "Gamma 2.8";
#if LIBAVCODEC_VERSION_MAJOR > 53
        case AVCOL_TRC_SMPTE240M:
            return "Smpte 240M";
#endif
#if LIBAVCODEC_VERSION_MAJOR > 54
#ifdef AVCOL_TRC_SMPTE170M
        case AVCOL_TRC_SMPTE170M:
            return "Rec 601 / ITU-R BT601-6 525 or 625 / ITU-R BT1358 525 or 625 / ITU-R BT1700 NTSC";
#endif
#ifdef AVCOL_TRC_LINEAR
        case AVCOL_TRC_LINEAR:
            return "Linear transfer characteristics";
#endif
#ifdef AVCOL_TRC_LOG
        case AVCOL_TRC_LOG:
            return "Logarithmic transfer characteristic (100:1 range)";
#endif
#ifdef AVCOL_TRC_LOG_SQRT
        case AVCOL_TRC_LOG_SQRT:
            return "Logarithmic transfer characteristic (100 * Sqrt( 10 ) : 1 range)";
#endif
#ifdef AVCOL_TRC_IEC61966_2_4
        case AVCOL_TRC_IEC61966_2_4:
            return "IEC 61966-2-4";
#endif
#ifdef AVCOL_TRC_BT1361_ECG
        case AVCOL_TRC_BT1361_ECG:
            return "ITU-R BT1361 Extended Colour Gamut";
#endif
#ifdef AVCOL_TRC_IEC61966_2_1
        case AVCOL_TRC_IEC61966_2_1:
            return "IEC 61966-2-1 (sRGB or sYCC)";
#endif
#ifdef AVCOL_TRC_BT2020_10
        case AVCOL_TRC_BT2020_10:
            return "ITU-R BT2020 for 10 bit system";
#endif
#ifdef AVCOL_TRC_BT2020_12
        case AVCOL_TRC_BT2020_12:
            return "ITU-R BT2020 for 12 bit system";
#endif
#endif
        case AVCOL_TRC_NB:
            return "Not ABI";
        default:
            return "";
    }
}

std::string VideoProperties::getColorspace() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");

    switch(_codecContext->colorspace)
    {
        case AVCOL_SPC_RGB:
            return "RGB";
        case AVCOL_SPC_BT709:
            return "Rec 709";
        case AVCOL_SPC_UNSPECIFIED:
            return "unspecified";
        case AVCOL_SPC_FCC:
            return "Four CC";
        case AVCOL_SPC_BT470BG:
            return "BT470 (PAL - 625)";
        case AVCOL_SPC_SMPTE170M:
            return "Smpte 170M (NTSC)";
        case AVCOL_SPC_SMPTE240M:
            return "Smpte 240M";
#if LIBAVCODEC_VERSION_MAJOR > 53
        case AVCOL_SPC_YCOCG:
            return "Y Co Cg";
//#else
//		case AVCOL_SPC_YCGCO:
//		return "Y Cg Co";
#endif
#if LIBAVCODEC_VERSION_MAJOR > 54
#ifdef AVCOL_TRC_BT2020_12
        case AVCOL_SPC_BT2020_NCL:
            return "ITU-R BT2020 non-constant luminance system";
#endif
#ifdef AVCOL_TRC_BT2020_CL
        case AVCOL_SPC_BT2020_CL:
            return "ITU-R BT2020 constant luminance system";
#endif
#endif
        case AVCOL_SPC_NB:
            return "Not ABI";
        default:
            return "";
    }
}

std::string VideoProperties::getColorRange() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");

    switch(_codecContext->color_range)
    {
        case AVCOL_RANGE_UNSPECIFIED:
            return "unspecified";
        case AVCOL_RANGE_MPEG:
            return "Limited"; //< legal TV range: 16-235 with 8bits
        case AVCOL_RANGE_JPEG:
            return "Full"; // full range: 0-255 with 8bits
        case AVCOL_RANGE_NB:
            return "Not ABI";
        default:
            return "";
    }
}

std::string VideoProperties::getColorPrimaries() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");

    switch(_codecContext->color_primaries)
    {
        case AVCOL_PRI_BT709:
            return "Rec 709";
        case AVCOL_PRI_UNSPECIFIED:
            return "unspecified";
        case AVCOL_PRI_BT470M:
            return "BT 470M";
        case AVCOL_PRI_BT470BG:
            return "Rec 601 (PAL & SECAM)";
        case AVCOL_PRI_SMPTE170M:
            return "Rec 601 (NTSC)";
        case AVCOL_PRI_SMPTE240M:
            return "Smpte 240 (NTSC)";
        case AVCOL_PRI_FILM:
            return "Film";
#if LIBAVCODEC_VERSION_MAJOR > 54
#ifdef AVCOL_TRC_BT2020_CL
        case AVCOL_PRI_BT2020:
            return "ITU-R BT2020";
#endif
#endif
        case AVCOL_PRI_NB:
            return "Not ABI";
        default:
            return "";
    }
}

std::string VideoProperties::getChromaSampleLocation() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");

    switch(_codecContext->chroma_sample_location)
    {
        case AVCHROMA_LOC_UNSPECIFIED:
            return "unspecified";
        case AVCHROMA_LOC_LEFT:
            return "left (mpeg2/4, h264 default)";
        case AVCHROMA_LOC_CENTER:
            return "center (mpeg1, jpeg, h263)";
        case AVCHROMA_LOC_TOPLEFT:
            return "top left";
        case AVCHROMA_LOC_TOP:
            return "top";
        case AVCHROMA_LOC_BOTTOMLEFT:
            return "bottom left";
        case AVCHROMA_LOC_BOTTOM:
            return "bottom";
        case AVCHROMA_LOC_NB:
            return "Not ABI";
        default:
            return "";
    }
}

std::string VideoProperties::getFieldOrder() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");

    switch(_codecContext->field_order)
    {
        case AV_FIELD_UNKNOWN:
            return "unknown";
        case AV_FIELD_PROGRESSIVE:
            return "progressive";
        case AV_FIELD_TT:
            return "top top";
        case AV_FIELD_BB:
            return "bottom bottom";
        case AV_FIELD_TB:
            return "top bottom";
        case AV_FIELD_BT:
            return "bottom top";
        default:
            return "";
    }
}

int64_t VideoProperties::getStartTimecode() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");
    return _firstGopTimeCode;
}

std::string VideoProperties::getStartTimecodeString() const
{
    int64_t startTimeCode = getStartTimecode();
    std::ostringstream os;
    if(startTimeCode == -1)
        os << "unset";
    else
    {
        os << std::setfill('0');
        os << std::setw(2) << (startTimeCode >> 19 & 0x1f) << ":"; // 5-bit hours
        os << std::setw(2) << (startTimeCode >> 13 & 0x3f) << ":"; // 6-bit minutes
        os << std::setw(2) << (startTimeCode >> 6 & 0x3f);         // 6-bit seconds
        os << (startTimeCode & 1 << 24 ? ';' : ':');               // 1-bit drop flag
        os << std::setw(2) << (startTimeCode & 0x3f);              // 6-bit frames
    }
    return os.str();
}

Rational VideoProperties::getSar() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");

    Rational sar = {
        _codecContext->sample_aspect_ratio.num, _codecContext->sample_aspect_ratio.den,
    };
    return sar;
}

Rational VideoProperties::getDar() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");

    int darNum, darDen;
    av_reduce(&darNum, &darDen, _codecContext->width * getSar().num, _codecContext->height * getSar().den, 1024 * 1024);

    Rational dar = {
        darNum, darDen,
    };
    return dar;
}

size_t VideoProperties::getBitRate() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");

    // return bit rate of stream if present or VBR mode
    if(_codecContext->bit_rate || _codecContext->rc_max_rate)
        return _codecContext->bit_rate;
    LOG_WARN("The bitrate of the stream '" << _streamIndex << "' of file '" << _formatContext->url << "' is unknown.")

    if(_levelAnalysis == eAnalyseLevelHeader)
    {
        LOG_INFO("Need a deeper analysis: see eAnalyseLevelFirstGop.")
        return 0;
    }

    LOG_INFO("Estimate the video bitrate from the first GOP.")
    size_t gopFramesSize = 0;
    for(size_t picture = 0; picture < _gopStructure.size(); ++picture)
    {
        gopFramesSize += _gopStructure.at(picture).second;
    }
    return (gopFramesSize / getGopSize()) * 8 * getFps();
}

size_t VideoProperties::getMaxBitRate() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");

    if (_codecContext->rc_max_rate == 0
        && _codecContext->coded_side_data
        && _codecContext->coded_side_data->type == AV_PKT_DATA_CPB_PROPERTIES) {
        const AVCPBProperties* prop = (AVCPBProperties*) _codecContext->coded_side_data->data;
        return prop->max_bitrate;
    }

    return _codecContext->rc_max_rate;
}

size_t VideoProperties::getMinBitRate() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");

    if (_codecContext->rc_max_rate == 0
        && _codecContext->coded_side_data
        && _codecContext->coded_side_data->type == AV_PKT_DATA_CPB_PROPERTIES) {
        const AVCPBProperties* prop = (AVCPBProperties*) _codecContext->coded_side_data->data;
        return prop->min_bitrate;
    }

    return _codecContext->rc_min_rate;
}

size_t VideoProperties::getNbFrames() const
{
    if(!_formatContext)
        throw std::runtime_error("unknown format context");

    const size_t nbFrames = _formatContext->streams[_streamIndex]->nb_frames;
    if(nbFrames)
        return nbFrames;
    LOG_WARN("The number of frames of the stream '" << _streamIndex << "' of file '" << _formatContext->url
                                                    << "' is unknown.")

    if(_levelAnalysis == eAnalyseLevelHeader)
    {
        LOG_INFO("Need a deeper analysis: see eAnalyseLevelFirstGop.")
        return 0;
    }

    if(! _nbFrames)
    {
        LOG_INFO("Estimate the number of frames from the fps and the duration.")
        return getFps() * getDuration();
    }
    LOG_INFO("Get the exact number of frames.")
    return _nbFrames;
}

size_t VideoProperties::getTicksPerFrame() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");
    return _codecContext->ticks_per_frame;
}

size_t VideoProperties::getWidth() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");
    return _codecContext->width;
}

size_t VideoProperties::getHeight() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");
    return _codecContext->height;
}

size_t VideoProperties::getDtgActiveFormat() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");
    // return _codecContext->dtg_active_format;
    return 0;
}

size_t VideoProperties::getReferencesFrames() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");
    return _codecContext->refs;
}

int VideoProperties::getProfile() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");
    return _codecContext->profile;
}

int VideoProperties::getLevel() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");
    return _codecContext->level;
}

float VideoProperties::getFps() const
{
    if(! _formatContext)
        throw std::runtime_error("unknown format context");

    if(_formatContext->streams[_streamIndex]->avg_frame_rate.den)
        return av_q2d(_formatContext->streams[_streamIndex]->avg_frame_rate);
    return av_q2d(av_inv_q(getTimeBase()));
}

float VideoProperties::getDuration() const
{
    const float duration = StreamProperties::getDuration();
    if(duration != 0)
        return duration;
    LOG_WARN("The duration of the stream '" << _streamIndex << "' of file '" << _formatContext->url << "' is unknown.")

    if(_levelAnalysis == eAnalyseLevelHeader)
    {
        LOG_INFO("Need a deeper analysis: see eAnalyseLevelFirstGop.")
        return 0;
    }

    if(! _nbFrames)
    {
        LOG_INFO("Estimate the duration from the file size and the bitrate.")
        const size_t bitRate = getBitRate();
        if(bitRate)
            return _fileProperties->getFileSize() / bitRate * 8;
    }
    LOG_INFO("Get the exact duration from the number of frames and the fps.")
    return _nbFrames / getFps();
}

bool VideoProperties::hasBFrames() const
{
    if(!_codecContext)
        throw std::runtime_error("unknown codec context");
    return (bool)_codecContext->has_b_frames;
}

// CODEC_FLAG_CLOSED_GOP is superior of INT_MAX, and _codecContext->flags is an int
// => Need a patch from FFmpeg
// bool VideoProperties::isClosedGop() const
//{
//	if( ! _codecContext )
//		throw std::runtime_error( "unknown codec context" );
//	return ( _codecContext->flags & CODEC_FLAG_CLOSED_GOP ) == CODEC_FLAG_CLOSED_GOP;
//}

bool VideoProperties::isInterlaced() const
{
    if(_levelAnalysis < eAnalyseLevelFirstGop)
        throw std::runtime_error("Need a deeper analysis: see eAnalyseLevelFirstGop.");
    return _isInterlaced;
}

bool VideoProperties::isTopFieldFirst() const
{
    if(_levelAnalysis < eAnalyseLevelFirstGop)
        throw std::runtime_error("Need a deeper analysis: see eAnalyseLevelFirstGop.");
    return _isTopFieldFirst;
}

size_t VideoProperties::getGopSize() const
{
    if(_levelAnalysis < eAnalyseLevelFirstGop)
        throw std::runtime_error("Need a deeper analysis: see eAnalyseLevelFirstGop.");
    return _gopSize;
}

std::vector<std::pair<char, int> > VideoProperties::getGopStructure() const
{
    if(_levelAnalysis < eAnalyseLevelFirstGop)
        throw std::runtime_error("Need a deeper analysis: see eAnalyseLevelFirstGop.");
    return _gopStructure;
}

size_t VideoProperties::analyseGopStructure(IProgress& progress)
{
    if(! _formatContext || ! _codecContext || ! _codec)
        return 0;
    if(! _codecContext->width || ! _codecContext->height)
        return 0;

    InputFile& file = const_cast<InputFile&>(_fileProperties->getInputFile());
    // Get the stream
    IInputStream& stream = file.getStream(_streamIndex);
    stream.activate();
    // Create a decoder
    VideoDecoder decoder(static_cast<InputStream&>(stream));
    VideoFrame frame(VideoFrameDesc(getWidth(), getHeight(), getPixelFormatName(getPixelProperties().getAVPixelFormat())), false);

    size_t nbDecodedFrames = 0;
    int positionOfFirstKeyFrame = -1;
    int positionOfLastKeyFrame = -1;
    while(decoder.decodeNextFrame(frame))
    {
        AVFrame& avFrame = frame.getAVFrame();

        _gopStructure.push_back(
            std::make_pair(av_get_picture_type_char(avFrame.pict_type), avFrame.pkt_size));
        _isInterlaced = avFrame.interlaced_frame;
        _isTopFieldFirst = avFrame.top_field_first;
        if(avFrame.pict_type == AV_PICTURE_TYPE_I)
        {
            if(positionOfFirstKeyFrame == -1)
                positionOfFirstKeyFrame = nbDecodedFrames;
            else
                positionOfLastKeyFrame = nbDecodedFrames;
        }

        _gopSize = ++nbDecodedFrames;

        // If the first 2 key frames are found
        if(positionOfFirstKeyFrame != -1 && positionOfLastKeyFrame != -1)
        {
            // Set gop size as distance between these 2 key frames
            _gopSize = positionOfLastKeyFrame - positionOfFirstKeyFrame;
            // Update gop structure to keep only one gop
            while(_gopStructure.size() > _gopSize)
                _gopStructure.pop_back();
            break;
        }
    }

    // disable the stream
    stream.activate(false);

    // Check GOP size
    if(_gopSize <= 0)
    {
        throw std::runtime_error("Invalid GOP size when decoding the first data.");
    }
    return nbDecodedFrames;
}

size_t VideoProperties::analyseFull(IProgress& progress)
{
    const size_t nbDecodedFrames = analyseGopStructure(progress);

    if(! _fileProperties->isRawFormat())
    {
        LOG_INFO("No need to decode all the stream to get more information.")
        return nbDecodedFrames;
    }

    if(! _formatContext || ! _codecContext || ! _codec)
        return 0;
    if(! _codecContext->width || ! _codecContext->height)
        return 0;

    InputFile& file = const_cast<InputFile&>(_fileProperties->getInputFile());
    // Get the stream
    IInputStream& stream = file.getStream(_streamIndex);
    stream.activate();
    // Create a decoder
    VideoDecoder decoder(static_cast<InputStream&>(stream));
    VideoFrame frame(VideoFrameDesc(getWidth(), getHeight(), getPixelFormatName(getPixelProperties().getAVPixelFormat())), false);

    const size_t estimateNbFrames = getNbFrames();
    _nbFrames = nbDecodedFrames;
    while(decoder.decodeNextFrame(frame))
    {
        progress.progress(++_nbFrames, estimateNbFrames);
    }

    // disable the stream
    stream.activate(false);

    // Check GOP size
    if(_nbFrames <= 0)
    {
        throw std::runtime_error("Invalid number of frames when decoding the video stream.");
    }
    return _nbFrames;
}

PropertyVector& VideoProperties::fillVector(PropertyVector& data) const
{
    // Add properties of base class
    PropertyVector basedProperty;
    StreamProperties::fillVector(basedProperty);
    data.insert(data.begin(), basedProperty.begin(), basedProperty.end());

    addProperty(data, "profile", &VideoProperties::getProfile);
    addProperty(data, "profileName", &VideoProperties::getProfileName);
    addProperty(data, "level", &VideoProperties::getLevel);
    addProperty(data, "startTimecode", &VideoProperties::getStartTimecodeString);
    addProperty(data, "width", &VideoProperties::getWidth);
    addProperty(data, "height", &VideoProperties::getHeight);
    addProperty(data, "pixelAspectRatio", &VideoProperties::getSar);
    addProperty(data, "displayAspectRatio", &VideoProperties::getDar);
    addProperty(data, "dtgActiveFormat", &VideoProperties::getDtgActiveFormat);
    addProperty(data, "colorTransfert", &VideoProperties::getColorTransfert);
    addProperty(data, "colorspace", &VideoProperties::getColorspace);
    addProperty(data, "colorRange", &VideoProperties::getColorRange);
    addProperty(data, "colorPrimaries", &VideoProperties::getColorPrimaries);
    addProperty(data, "chromaSampleLocation", &VideoProperties::getChromaSampleLocation);
    addProperty(data, "fieldOrder", &VideoProperties::getFieldOrder);
    addProperty(data, "fps", &VideoProperties::getFps);
    addProperty(data, "nbFrames", &VideoProperties::getNbFrames);
    addProperty(data, "ticksPerFrame", &VideoProperties::getTicksPerFrame);
    addProperty(data, "bitRate", &VideoProperties::getBitRate);
    addProperty(data, "maxBitRate", &VideoProperties::getMaxBitRate);
    addProperty(data, "minBitRate", &VideoProperties::getMinBitRate);
    addProperty(data, "hasBFrames", &VideoProperties::hasBFrames);
    addProperty(data, "referencesFrames", &VideoProperties::getReferencesFrames);

    // Add properties available when decode first gop
    if(_levelAnalysis < eAnalyseLevelFirstGop)
    {
        detail::add(data, "gopSize", detail::propertyValueIfError);
        detail::add(data, "gop", detail::propertyValueIfError);
        detail::add(data, "interlaced", detail::propertyValueIfError);
        detail::add(data, "topFieldFirst", detail::propertyValueIfError);
    }
    else
    {
        addProperty(data, "gopSize", &VideoProperties::getGopSize);

        std::stringstream gop;
        for(size_t frameIndex = 0; frameIndex < _gopStructure.size(); ++frameIndex)
        {
            gop << _gopStructure.at(frameIndex).first;
            gop << "(";
            gop << _gopStructure.at(frameIndex).second;
            gop << ")";
            gop << " ";
        }
        detail::add(data, "gop", gop.str());
        // detail::add( data, "isClosedGop", isClosedGop() );

        addProperty(data, "interlaced ", &VideoProperties::isInterlaced);
        addProperty(data, "topFieldFirst", &VideoProperties::isTopFieldFirst);
    }

    // Add properties of the pixel
    PropertyVector pixelProperties;
    _pixelProperties.fillVector(pixelProperties);
    data.insert(data.end(), pixelProperties.begin(), pixelProperties.end());

    return data;
}

std::ostream& operator<<(std::ostream& flux, const VideoProperties& videoProperties)
{
    flux << std::left;
    flux << detail::separator << " Video stream " << detail::separator << std::endl;

    PropertyVector properties = videoProperties.asVector();
    for(PropertyVector::iterator it = properties.begin(); it != properties.end(); ++it)
    {
        flux << std::setw(detail::keyWidth) << it->first << ": " << it->second << std::endl;
    }

    return flux;
}
}
