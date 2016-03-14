#include <Python.h>
#include <iostream>
#include <stdbool.h>
#include <string>
#include <netinet/in.h>
#include <regex.h>
typedef unsigned char   u1;
typedef unsigned short  u2;
typedef unsigned int u4;
//struct ClassFile
//{
//  u4 magic;                                 //识别Class文件格式，具体值为0xCAFEBABE，
//  u2 minor_version;            // Class文件格式副版本号，
//  u2 major_version;            // Class文件格式主版本号，
//  u2 constant_pool_count; //  常数表项个数，
//  cp_info **constant_pool;// 常数表，又称变长符号表，
//  u2 access_flags;               //Class的声明中使用的修饰符掩码，
//  u2 this_class;                   //常数表索引，索引内保存类名或接口名，
//  u2 super_class;                //常数表索引，索引内保存父类名，
//  u2 interfaces_count;        //超接口个数，
//  u2 *interfaces;                 //常数表索引，各超接口名称，
//  u2 fields_count;       //类的域个数，
//  field_info **fields;          //域数据，包括属性名称索引，
////域修饰符掩码等，
//  u2 methods_count;          //方法个数，
//  method_info **methods;//方法数据，包括方法名称索引，方法修饰符掩码等，
//  u2 attributes_count;        //类附加属性个数，
//  attribute_info **attributes; //类附加属性数据，包括源文件名等。
//};
#pragma pack(1)
typedef struct Header
{
    u4 magic;
    u2 minor_version;
    u2 major_version;
    u2 constant_pool_count;
}Header;
typedef struct Item
{
  u1 tag;       //常数表数据类型
  u1 *info;   //常数表数据
}Item;
typedef struct StringInfo
{ 
  u1 tag;
  u2 string_index;
}StringInfo;
typedef struct UTFInfo {
  u1 tag;
  u2 length;
}UTFInfo;
const int TypeString = 8;
const int TypeUtf = 1;
static bool compiled = false;
  const size_t nmatch = 1;
  regmatch_t pm[1];
  regex_t reg;
static bool regexMatch(const std::string& str)
{
  if(!compiled) {
      regcomp(&reg, "\\w{2,20}@\\w{2,20}", REG_EXTENDED|REG_NOSUB);
      compiled = true;
  }
  return REG_NOMATCH != regexec(&reg, str.c_str(), nmatch, pm, REG_NOTBOL);
}
void findKey(const char * path, std::string& result)
{
    FILE* f = fopen(path, "rb");
    if (f == 0) {
      std::cout << "can not find file " << path << std::endl;
      return;
    }
    fseek(f, 0, SEEK_END);
    long len = ftell(f);
    fseek(f, 0, SEEK_SET);
    char * buffer = new char[len];
    long ret = fread(buffer, sizeof(char), len, f);
    fclose(f);
    if(ret != len){
        std::cout << "read file length error!" << std::endl;
        return;
    }

    Header* header = (Header*)buffer;
    header->constant_pool_count = ntohs(header->constant_pool_count);
    const char* ptr = buffer;
    ptr += sizeof(Header);
    int i = 1;
    for (; i < header->constant_pool_count; ++i) {
      char tag = *ptr;
      int to_seek = 0;
      switch(tag)
      {
          case 1://UTF8
              to_seek = -1;
              break;
          case 3://Integer
          case 4://Float
              to_seek = 4;
              break;
          case 5://Long
          case 6://Double
              to_seek = 8;
              break;
          case 7://Class
              to_seek = 2;
              break;
          case 8://String
              to_seek = 2;
              break;
          case 9://Fieldref
          case 10://Methodref
          case 11://InterfaceMethodref
              to_seek = 4;
              break;
          case 12://NameAndType
              to_seek = 4;
              break;
          case 15://MethodHandle
              to_seek = 3;
              break;
          case 16://MethodType
              to_seek = 2;
              break;
          case 18://InvokeDynamic
              to_seek = 4;
              break;

        default:
              //std::cout << "default--------------" << (int)tag << std::endl;
              break;
          
      }
      ptr ++;
      ptr += to_seek;
      if(tag == TypeUtf) {
        UTFInfo * utfInfo = (UTFInfo*) ptr;
        utfInfo->length = ntohs(utfInfo->length);
        ptr += sizeof(UTFInfo);
        std::string key(ptr, utfInfo->length);
        ptr += utfInfo->length;
        if(key.size() > 2 && key.size() < 50){
          bool match = regexMatch(key);
          if(match){
            result +="####";
            result += key;
          }
        }
      }

    }
    delete [] buffer;
    return;
}

static PyObject* FindKey(PyObject *self, PyObject *args)
{
  char * path;
  if (!PyArg_ParseTuple(args, "s", &path)) {
    return Py_BuildValue("s", "abc");
  }
  std::string keys;
  findKey(path, keys);
  return Py_BuildValue("s", keys.c_str());
}
#ifdef __cplusplus
extern "C" {
#endif

static PyMethodDef find_methods[] = {
    {"FindKey", FindKey, METH_VARARGS, "find constant key string from class file"},
    {0, 0}
};

void initFindKey()
{
    Py_InitModule3("FindKey",  find_methods, "find constant key string from class file!");
}
#ifdef __cplusplus
}
#endif
