#include "JsonWriter.hpp"

#include <string>
#include <sstream>

namespace avtranscoder
{
namespace json
{

std::string JsonStreamWriter::escapeJsonString(const std::string& input)
{
    std::ostringstream ss;
    for(std::string::const_iterator iter = input.begin(); iter != input.end(); iter++)
    {
        switch(*iter)
        {
            case '\\':
                ss << "\\\\";
                break;
            default:
                ss << *iter;
                break;
        }
    }
    return ss.str();
}

template <>
JsonObjectStreamWriter& JsonObjectStreamWriter::operator<<(const std::pair<const char*, const char*> pair)
{
    std::string first(pair.first);
    std::string second(pair.second);
    addSep() << escapeJsonString(first).c_str() << ':' << escapeJsonString(second).c_str();
    return *this;
}

template <>
JsonStreamWriter& JsonStreamWriter::operator<<(bool value)
{
    stream << (value ? "true" : "false");
    return *this;
}
template <>
JsonStreamWriter& JsonStreamWriter::operator<<(const char* string)
{
    stream << '"' << string << '"';
    return *this;
}
template <>
JsonStreamWriter& JsonStreamWriter::operator<<(JsonNull)
{
    stream << "null";
    return *this;
}
}
}
