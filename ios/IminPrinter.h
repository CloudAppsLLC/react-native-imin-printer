
#ifdef RCT_NEW_ARCH_ENABLED
#import "RNIminPrinterSpec.h"

@interface IminPrinter : NSObject <NativeIminPrinterSpec>
#else
#import <React/RCTBridgeModule.h>

@interface IminPrinter : NSObject <RCTBridgeModule>
#endif

@end
