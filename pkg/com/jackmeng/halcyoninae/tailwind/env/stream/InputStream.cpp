#include "InputStream.hpp"

#include <AvTranscoder/file/InputFile.hpp>

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
}

#include <stdexcept>
#include <cassert>

namespace avtranscoder
{

InputStream::InputStream(InputFile& inputFile, const size_t streamIndex)
    : IInputStream()
    , _inputFile(&inputFile)
    , _codec(NULL)
    , _streamCache()
    , _streamIndex(streamIndex)
    , _isActivated(false)
{
    AVStream& avStream = _inputFile->getFormatContext().getAVStream(_streamIndex);
    AVCodecParameters* codecParameters = avStream.codecpar;

    const AVCodec* codec = avcodec_find_decoder(codecParameters->codec_id);
    AVCodecContext* context = avcodec_alloc_context3(codec);

    int ret = avcodec_parameters_to_context(context, codecParameters);
    context->time_base = avStream.time_base;

    if (ret < 0)
        throw std::runtime_error("Failed to copy decoder parameters to input stream context");

    switch(context->codec_type)
    {
        case AVMEDIA_TYPE_VIDEO:
        {
            _codec = new VideoCodec(eCodecTypeDecoder, *context);
            break;
        }
        case AVMEDIA_TYPE_AUDIO:
        {
            _codec = new AudioCodec(eCodecTypeDecoder, *context);
            break;
        }
        case AVMEDIA_TYPE_DATA:
        {
            _codec = new DataCodec(eCodecTypeDecoder, *context);
            break;
        }
        default:
            break;
    }
}

InputStream::~InputStream()
{
    delete _codec;
}

bool InputStream::readNextPacket(CodedData& data)
{
    if(!_isActivated)
        throw std::runtime_error("Can't read packet on non-activated input stream.");

    // if packet is already cached
    if(!_streamCache.empty())
    {
        LOG_DEBUG("Get packet of '" << _inputFile->getFilename() << "' (stream " << _streamIndex << ") from the cache")
        data.copyData(_streamCache.front().getData(), _streamCache.front().getSize());
        _streamCache.pop();
    }
    // else read next packet
    else
    {
        LOG_DEBUG("Read next packet of '" << _inputFile->getFilename() << "' (stream " << _streamIndex << ")")
        return _inputFile->readNextPacket(data, _streamIndex) && _streamCache.empty();
    }

    return true;
}

VideoCodec& InputStream::getVideoCodec()
{
    assert(_streamIndex <= _inputFile->getFormatContext().getNbStreams());

    if(getProperties().getStreamType() != AVMEDIA_TYPE_VIDEO)
    {
        throw std::runtime_error("unable to get video descriptor on non-video stream");
    }

    return *static_cast<VideoCodec*>(_codec);
}

AudioCodec& InputStream::getAudioCodec()
{
    assert(_streamIndex <= _inputFile->getFormatContext().getNbStreams());

    if(getProperties().getStreamType() != AVMEDIA_TYPE_AUDIO)
    {
        throw std::runtime_error("unable to get audio descriptor on non-audio stream");
    }

    return *static_cast<AudioCodec*>(_codec);
}

DataCodec& InputStream::getDataCodec()
{
    assert(_streamIndex <= _inputFile->getFormatContext().getNbStreams());

    if(getProperties().getStreamType() != AVMEDIA_TYPE_DATA)
    {
        throw std::runtime_error("unable to get data descriptor on non-data stream");
    }

    return *static_cast<DataCodec*>(_codec);
}

const StreamProperties& InputStream::getProperties() const
{
    return _inputFile->getProperties().getStreamPropertiesWithIndex(_streamIndex);
}

void InputStream::addPacket(const AVPacket& packet)
{
    // Do not cache data if the stream is declared as unused in process
    if(!_isActivated)
    {
        LOG_DEBUG("Do not add a packet data for the stream " << _streamIndex << " to the cache: stream not activated")
        return;
    }

    LOG_DEBUG("Add a packet data for the stream " << _streamIndex << " to the cache");
    CodedData codedData;
    codedData.copyData(packet.data, packet.size);
    _streamCache.push(codedData);
}

void InputStream::clearBuffering()
{
    _streamCache = std::queue<CodedData>();
}
}
