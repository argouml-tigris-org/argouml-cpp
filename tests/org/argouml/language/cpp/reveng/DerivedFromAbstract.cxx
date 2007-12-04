// $Id$
// Copyright (c) 2007 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason. IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

// This example shows that the reveng module supports revenging a class which 
// is derived from an abstract class. It also shows correct protected, private 
// and public access specifiers are correctly reversed.
// Use of modifiers (e.g., unsigned) is included, as of pointers, references 
// and of user types as parameters and return values.

struct Dummy {
    signed char c;
};

class Base {
public:
    virtual unsigned int foo(Base& other) = 0;
protected:
    unsigned long ui;
    // and a protected method, to which the derived class still has access
    Dummy makeMeADummy() { 
       Dummy d;
       d.c = '4';
       helperMethod(&d.c);
       return d;
    }
private:
    void helperMethod(signed char* cstr);
};

class Derived: public Base {
    Dummy* pDum;
public:
    Derived(): Base(), pDum(0) {
       ui = 0;
    }
    ~Derived() {
        if (pDum) delete pDum;
    }
    unsigned int foo(Base& other) {
       if (!pDum) {
           pDum = new Dummy;
           *pDum = makeMeADummy();
       }
       // the next would cause problems if this were ever to run
       return 3 * 2 + pDum->c + other.foo(*this);
    }
};

void Base::helperMethod(signed char* cstr) {
    // dumb thing, simply does nothing
}
