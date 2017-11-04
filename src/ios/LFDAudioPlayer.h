//
//  LFDAudioPlayer.h
//  LFD Audio Player
//
//  Created by -philipp on 26.06.14.
//  Copyright (c) 2014 Quatur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>

@protocol LFDAudioPlayerDelegate

- (void) startedPlaying:(AVPlayerItem*)playerItem;
- (void) stoppedPlaying;
- (void) failedPlaying:(NSError*)error;
- (void) playingTitle:(NSString*)title;

@end

@interface LFDAudioPlayer : NSObject

@property (weak, nonatomic) id<LFDAudioPlayerDelegate>playerDelegate;

+ (LFDAudioPlayer*)sharedInstance;

- (void)play:(NSURL*)stationUrl;
- (void)pause;
- (bool)isPlaying;
- (void)slowFadeOut;
- (void)quickFadeOut;

@end
