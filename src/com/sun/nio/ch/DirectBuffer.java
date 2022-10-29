package com.sun.nio.ch;

import com.sun.misc.Cleaner;

public interface DirectBuffer
{
    long address();
    
    Object attachment();
    
    Cleaner cleaner();
}
