//
//  HTTPUtil2.h
//  TJClient
//
//  Created by Katsunobu Ishida on 10/06/28.
//  Copyright 2010 Katsunobu Ishida. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface HTTPUtil : NSObject {
 @private
	NSMutableData *responseData;
	id resultHandler;
	SEL resultSelector;
}

- (void)sendAsyncGetRequestWithURL:(NSURL*)url 
				   timeoutInterval:(NSTimeInterval)timeout
								to:(id)delegate 
						  selector:(SEL)selector;
@end
