%apply char * { unsigned char * };

%{
#include <AvTranscoder/data/coded/CodedData.hpp>
#include <AvTranscoder/data/decoded/IFrame.hpp>
#include <AvTranscoder/data/decoded/VideoFrame.hpp>
#include <AvTranscoder/data/decoded/AudioFrame.hpp>
%}

%include <AvTranscoder/data/coded/CodedData.hpp>
%include <AvTranscoder/data/decoded/IFrame.hpp>
%include <AvTranscoder/data/decoded/VideoFrame.hpp>
%include <AvTranscoder/data/decoded/AudioFrame.hpp>
