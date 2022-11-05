#ifndef _AV_TRANSCODER_AUDIOREADER_HPP
#define _AV_TRANSCODER_AUDIOREADER_HPP

#include "IReader.hpp"

#include <AvTranscoder/file/InputFile.hpp>
#include <AvTranscoder/properties/AudioProperties.hpp>

namespace avtranscoder
{

class AvExport AudioReader : public IReader
{
public:
    //@{
    // @note Transform the input stream to s16 sample format (to listen).
    // @see updateOutput
    AudioReader(const InputStreamDesc& inputDesc);
    //@}

    ~AudioReader();

    /**
     * @brief Update sample rate, number of channels and sample format of the output.
     * @note Will transform the decoded data when read the stream.
     */
    void updateOutput(const size_t sampleRate, const size_t nbChannels, const std::string& sampleFormat);

    //@{
    // @brief Output info
    size_t getOutputSampleRate() const { return _outputSampleRate; }
    size_t getOutputNbChannels() const { return _outputNbChannels; }
    AVSampleFormat getOutputSampleFormat() const { return _outputSampleFormat; }
    //@}

    // @brief Get source audio properties
    const AudioProperties* getSourceAudioProperties() const { return _audioStreamProperties; }

private:
    void init();

private:
    const AudioProperties* _audioStreamProperties; ///< Properties of the source audio stream read (no ownership, has link)

    //@{
    // @brief Output info
    size_t _outputSampleRate;
    size_t _outputNbChannels;
    AVSampleFormat _outputSampleFormat;
    //@}
};
}

#endif
