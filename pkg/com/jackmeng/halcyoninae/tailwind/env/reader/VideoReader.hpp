#ifndef _AV_TRANSCODER_VIDEOREADER_HPP
#define _AV_TRANSCODER_VIDEOREADER_HPP

#include "IReader.hpp"

#include <AvTranscoder/file/InputFile.hpp>
#include <AvTranscoder/properties/PixelProperties.hpp>
#include <AvTranscoder/properties/VideoProperties.hpp>

namespace avtranscoder
{

class AvExport VideoReader : public IReader
{
public:
    //@{
    // @note Transform the input stream to rgb24 pixel format (to display).
    // @see updateOutput
    VideoReader(const InputStreamDesc& inputDesc);
    //@}

    ~VideoReader();

    /**
     * @brief Update width, height and pixelFormat of the output.
     * @note Will transform the decoded data when read the stream.
     */
    void updateOutput(const size_t width, const size_t height, const std::string& pixelFormat);

    //@{
    // @brief Output info
    size_t getOutputWidth() const { return _outputWidth; }
    size_t getOutputHeight() const { return _outputHeight; }
    size_t getOutputNbComponents() const { return _outputPixelProperties.getNbComponents(); }
    size_t getOutputBitDepth() const { return _outputPixelProperties.getBitsPerPixel(); }
    AVPixelFormat getOutputPixelFormat() const { return _outputPixelProperties.getAVPixelFormat(); }
    //@}

    // @brief Get source video properties
    const VideoProperties* getSourceVideoProperties() const { return _videoStreamProperties; }

private:
    void init();

private:
    const VideoProperties* _videoStreamProperties; ///< Properties of the source video stream read (no ownership, has link)

    //@{
    // @brief Output info
    size_t _outputWidth;
    size_t _outputHeight;
    PixelProperties _outputPixelProperties;
    //@}
};
}

#endif
