//
//  TJClientViewController.h
//  TJClient
//
//  Created by Katsunobu Ishida on 10/06/27.
//  Copyright Katsunobu Ishida 2010. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "InfoViewController.h"

@interface TJClientViewController : UIViewController <UITextFieldDelegate, UIAccelerometerDelegate> {
 @private
    UITextField *urlTextField;
    UILabel *connectionStatusLabel;
    UILabel *accelerometerStatusLabel;
	UIViewController *infoViewController;
	UIAccelerometer *accelerometer;
	BOOL canSend;
}

@property (nonatomic, retain) IBOutlet UITextField *urlTextField;
@property (nonatomic, retain) IBOutlet UILabel *connectionStatusLabel;
@property (nonatomic, retain) IBOutlet UILabel *accelerometerStatusLabel;

- (IBAction)sendActionLeft:(id)sender;
- (IBAction)sendActionRight:(id)sender;
- (IBAction)sendActionUp:(id)sender;
- (IBAction)sendActionDown:(id)sender;
- (IBAction)sendActionZoomIn:(id)sender;
- (IBAction)sendActionZoomOut:(id)sender;
- (IBAction)sendActionJog:(id)sender;
- (IBAction)showInfoView:(id)sender;

@end

