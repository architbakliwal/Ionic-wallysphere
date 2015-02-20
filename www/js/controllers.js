angular.module('starter.controllers', [])

.controller('MainCtrl', function($scope, $ionicSideMenuDelegate) {
  $scope.settings = { frequency: 'FW', network: 'NW', onoff: true };

  $scope.toggleLeft = function() {
    $ionicSideMenuDelegate.toggleLeft();
  };
})

.controller('HomeCtrl', function($scope, $http) {

    $scope.wallpaperUrl = '';
    
    $scope.downloadwallpaper = function() {
        console.log('downloading wallpaper');

        var type = Math.floor((Math.random() * 2) + 1);
        switch (type) {
            case 1:
                $http.get('https://api.500px.com/v1/photos/search?term=night%2C%20sky%2C%20moon%2C%20stars&rpp=10&only=Landscapes%2C%20Nature&image_size=4&sort=highest_rating&consumer_key=QbKA0F86Jx6xvOpxzmciYlRbBQQoIykCuXFuFKOX').
                    success(function(data, status, headers, config) {

                        var which = Math.floor((Math.random() * 8) + 0);
                        console.log(data.photos[which].image_url);

                        $scope.wallpaperUrl = data.photos[which].image_url;
                        DownloadFile($scope.wallpaperUrl, 'Wallysphere', 'night');

                    }).
                    error(function(data, status, headers, config) {
                        console.log(JSON.stringify(data));
                    });
                break;
            case 2:
                $http.get('https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=2962d4c497bd4b64a58dbdfb31e0da27&tags=night%2C+sky%2C+moon%2C+stars&text=night+sky+stars+moon&sort=interestingness-desc&content_type=1&media=photos&extras=url_o&per_page=10&page=1&format=json&nojsoncallback=1').
                    success(function(data, status, headers, config) {
                        var which = Math.floor((Math.random() * 8) + 0);

                        console.log(data.photos.photo[which].title);

                        var flickrtitle = data.photos.photo[which].title
                        var flickrid = data.photos.photo[which].id;
                        var flickrfarmid = data.photos.photo[which].farm;
                        var flickrserverid = data.photos.photo[which].server;
                        var flickrsecret = data.photos.photo[which].secret;

                        var flickrUrl = "https://farm" + flickrfarmid + ".staticflickr.com/" + flickrserverid + "/" + flickrid + "_" + flickrsecret + "_b.jpg";

                        $scope.wallpaperUrl = flickrUrl;

                        console.log(flickrUrl);
                        DownloadFile($scope.wallpaperUrl, 'Wallysphere', 'night');

                    }).
                    error(function(data, status, headers, config) {
                        console.log(JSON.stringify(data));
                    });
                break;
        }
       
        // DownloadFile('https://www.google.co.in/images/srpr/logo11w.png', 'Wallysphere', 'morning');
        // DownloadFile($scope.wallpaperUrl, 'Wallysphere', 'morning');
    }

    $scope.setwallpaper = function() {

        SetWallpaper('morning');
    }

    $scope.getNotificationDetails = function() {
        window.plugin.notification.local.getScheduledIds(function (scheduledIds) {
            console.log('Scheduled IDs: ' + scheduledIds.join(' ,'));
        });
    }
})

.controller('SettingsCtrl', function($scope) {

    function notifications() {

        window.plugin.notification.local.cancelAll();

        var current = new Date(),
        now = new Date(),
        morning = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 8, 0, 0, 0).getTime(),
        morningmsg = 'Have a nice day.' + morning,
        afternoon = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 15, 0, 0, 0).getTime(),
        afternoonmsg = 'Bon apetite.' + afternoon,
        night = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 22, 0, 0, 0).getTime(),
        nightmsg = 'Thank you.' + night;


        var now                  = new Date().getTime(),
            _30_seconds_from_now = new Date(now + 30*1000);

        window.plugin.notification.local.add({
            id:      1,
            title:   'Downloading Wallpapers',
            message: 'This is a no show notification which on trigger download wallpaper',
            date:    _30_seconds_from_now,
            repeat: 'daily',
            autoCancel: true,
            isShow: false
        });

        window.plugin.notification.local.add({
            id: 2,
            title: 'Good Morning!!!',
            message: morningmsg,
            repeat: 'daily',
            date: new Date(morning),
            autoCancel: true
        });

        window.plugin.notification.local.add({
            id: 3,
            title: 'Good Afternoon!!!',
            message: afternoonmsg,
            repeat: 'daily',
            date: new Date(afternoon),
            autoCancel: true
        });

        window.plugin.notification.local.add({
            id: 4,
            title: 'Good Night!!!',
            message: nightmsg,
            repeat: 'daily',
            date: new Date(night),
            autoCancel: true
        });

        window.plugin.notification.local.onadd = function (id, state, json) {
            console.log(id);
        };

        window.plugin.notification.local.ontrigger = function (id, state, json) {
            // console.log('triggered: ' + id);
            if(id == '1') {
                // console.log('inside ontrigger 1');
                SetWallpaper('night');
                
            } else if(id == '2') {
                // console.log('inside ontrigger 2');
                SetWallpaper('morning');

            } else if(id == '3') {
                // console.log('inside ontrigger 3');
                
            } else if(id == '4') {
                // console.log('inside ontrigger 4');
                SetWallpaper('night');

            }
        };
   
    }

    $scope.showForm = true;

    $scope.frequencies = [
        { text: 'Every Day', value: 'FD' },
        { text: 'Every Sunday', value: 'FW' },
        { text: 'Every Alternate Sunday', value: 'FWA' },
        { text: 'First Day of Every Month', value: 'FM' }
    ];

    $scope.networks = [
        { text: 'Wifi and Mobile Data', value: 'NM' },
        { text: 'Wifi Only', value: 'NW' }
    ];

    $scope.change = function() {
        console.log(JSON.stringify($scope.settings));
    };
    $scope.onoff = function() {
        if(!$scope.settings.onoff) {
            window.plugin.notification.local.cancelAll();
            $scope.showForm = false;
        } else {
            $scope.showForm = true;
            notifications();
        }
        console.log(JSON.stringify($scope.settings));
    };
});
