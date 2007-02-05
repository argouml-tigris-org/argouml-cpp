// Issue #0006 example of some really simple example that fails to be 
// parsed. 
// The problem was in the implementation of the Ctor and Dtor parsing 
// which didn't supported definitions outside of the class definition.

class Test {

    public:
        Test();
        virtual ~Test();

};


Test::Test() {
}

Test::~Test() {
}

