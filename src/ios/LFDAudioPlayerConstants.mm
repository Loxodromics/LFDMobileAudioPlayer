//
//  LFDAudioPlayerConstants.mm
//  LFD Audio Player
//
//  Created by -philipp on 16.07.14.
//  Copyright (c) 2014 Quatur. All rights reserved.
//

#import "LFDAudioPlayerConstants.h"

NSString* const kPlayerStatus           = @"status";
NSString* const kPlayerTimedMetadata    = @"timedMetadata";
NSString* const kPlayerTitle            = @"title";
NSString* const kPlayerBufferEmpty      = @"playbackBufferEmpty";
NSString* const kPlayerLikelyToKeepUp   = @"playbackLikelyToKeepUp";
NSString* const kPlayerNewStation       = @"PlayerNewStation";
NSString* const kPlayerStationChanged   = @"PlayerStationChanged";
NSString* const kPlayerNextStation      = @"PlayerNextStation";
NSString* const kPlayerPrevStation      = @"PlayerPrevStation";
NSString* const kPlayerPlayPauseStation = @"PlayerPlayPauseStation";
NSString* const kPlayerFavoritesChanged = @"PlayerFavoritesChanged";
NSString* const kPlayerRecentlyChanged  = @"PlayerRecentlyChanged";
NSUInteger const kPlayerNumReconnects   = 10;
float const kPlayerSlowFadeOutTime      = 5.0f;
float const kPlayerQuickFadeOutTime     = 0.25f;
float const kPlayerQuickFadeInTime      = 0.5f;
