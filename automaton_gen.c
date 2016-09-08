#include <stdio.h>
#define read(x) scanf("%d",&x)

#define write(x) printf("%d\n",x)

#define print(x) printf(x)

void 
csc512state_0 ( void ) ;
void csc512state_1 ( void ) ;
void csc512state_2 ( void ) ;
void csc512state_3 ( void ) ;
int csc512getnextdigit ( void ) {
int csc512n ;
while ( 0 == 0 ) {
print ( "Give me a number (-1 to quit): " ) ;
read ( csc512n ) ;
if ( - 1 <= csc512n && 1 >= csc512n ) {
break ;
}
print ( "I need a number that's either 0 or 1.\n" ) ;
}
return csc512n ;
}
void csc512state_0 ( void ) {
int csc512a ;
csc512a = csc512getnextdigit ( ) ;
if ( - 1 == csc512a ) {
print ( "You gave me an even number of 0's.\n" ) ;
print ( "You gave me an even number of 1's.\n" ) ;
print ( "I therefore accept this input.\n" ) ;
return ;
}
if ( 0 == csc512a ) {
csc512state_2 ( ) ;
}
if ( 1 == csc512a ) {
csc512state_1 ( ) ;
}
}
void csc512state_1 ( void ) {
int csc512a ;
csc512a = csc512getnextdigit ( ) ;
if ( - 1 == csc512a ) {
print ( "You gave me an even number of 0's.\n" ) ;
print ( "You gave me an odd number of 1's.\n" ) ;
print ( "I therefore reject this input.\n" ) ;
return ;
}
if ( 0 == csc512a ) {
csc512state_3 ( ) ;
}
if ( 1 == csc512a ) {
csc512state_0 ( ) ;
}
}
void csc512state_2 ( void ) {
int csc512a ;
csc512a = csc512getnextdigit ( ) ;
if ( - 1 == csc512a ) {
print ( "You gave me an odd number of 0's.\n" ) ;
print ( "You gave me an even number of 1's.\n" ) ;
print ( "I therefore reject this input.\n" ) ;
return ;
}
if ( 0 == csc512a ) {
csc512state_0 ( ) ;
}
if ( 1 == csc512a ) {
csc512state_3 ( ) ;
}
}
void csc512state_3 ( void ) {
int csc512a ;
csc512a = csc512getnextdigit ( ) ;
if ( - 1 == csc512a ) {
print ( "You gave me an odd number of 0's.\n" ) ;
print ( "You gave me an odd number of 1's.\n" ) ;
print ( "I therefore reject this input.\n" ) ;
return ;
}
if ( 0 == csc512a ) {
csc512state_1 ( ) ;
}
if ( 1 == csc512a ) {
csc512state_2 ( ) ;
}
}
int main ( ) {
csc512state_0 ( ) ;
}
