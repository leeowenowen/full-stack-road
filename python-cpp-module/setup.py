from distutils.core import setup, Extension
setup(name='FindKey', version='1.0',  \
      ext_modules=[Extension('FindKey', sources=['main.cpp'], extra_compile_args=['-std=c++11'])])
