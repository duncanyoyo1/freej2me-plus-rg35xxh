STATIC_LINKING := 0
AR := ar

# RG35XXH uses aarch64 (ARM64)
ARCH := aarch64
TARGET_PLATFORM := linux

# System platform
system_platform := linux
EXE_EXT :=

CORE_DIR := .
TARGET_NAME := freej2me
LIBM := -lm

# Toolchain for RG35XXH
TOOLCHAIN_PATH := /home/clay/toolchain/bin
CC := $(TOOLCHAIN_PATH)/aarch64-buildroot-linux-gnu-gcc
CXX := $(TOOLCHAIN_PATH)/aarch64-buildroot-linux-gnu-g++
AR := $(TOOLCHAIN_PATH)/aarch64-buildroot-linux-gnu-ar

# Compiler flags for RG35XXH
CFLAGS := -Wall -Wno-unused-result -D__LIBRETRO__ -fPIC -O3
CXXFLAGS := -Wall -Wno-unused-result -D__LIBRETRO__ -fPIC -O3

# Linker flags for RG35XXH
LDFLAGS := $(LIBM) -Wl,-z,now

# Target specifics
EXT := so
TARGET := $(TARGET_NAME)_libretro.$(EXT)
SHARED := -shared -Wl,--version-script=$(CORE_DIR)/link.T -Wl,--no-undefined

SOURCES_C += freej2me_libretro.c

SOURCES_CXX +=

INCLUDES += -Ideps/libretro-common/include

OBJECTS := $(SOURCES_C:.c=.o) $(SOURCES_CXX:.cpp=.o)

all: $(TARGET)

$(TARGET): $(OBJECTS)
	$(CXX) -fPIC $(SHARED) $(INCLUDES) -o $@ $(OBJECTS) $(LDFLAGS)


%.o: %.c
	$(CC) $(INCLUDES) $(CFLAGS) -c -o $@ $<

%.o: %.cpp # Handle C++ files
	$(CXX) $(INCLUDES) $(CXXFLAGS) -c -o $@ $<

clean:
	rm -f $(OBJECTS) $(TARGET)
