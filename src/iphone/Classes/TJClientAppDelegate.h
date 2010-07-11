//
//  TJClientAppDelegate.h
//  TJClient
//
//  Created by Katsunobu Ishida on 10/06/27.
//  Copyright Katsunobu Ishida 2010. All rights reserved.
//

#import <UIKit/UIKit.h>

@class TJClientViewController;

@interface TJClientAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
    TJClientViewController *viewController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet TJClientViewController *viewController;

@end

