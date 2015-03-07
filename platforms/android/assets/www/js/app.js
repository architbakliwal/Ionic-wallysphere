// Ionic Starter App

// angular.module is a global place for creating, registering and retrieving Angular modules
// 'starter' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'
// 'starter.controllers' is found in controllers.js
angular.module('starter', ['ionic', 'starter.controllers'])

.run(function($ionicPlatform, $ionicPopup, $rootScope) {
    $ionicPlatform.ready(function() {
        // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
        // for form inputs)
        if (window.cordova && window.cordova.plugins.Keyboard) {
            cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
        }
        if (window.StatusBar) {
            // org.apache.cordova.statusbar required
            StatusBar.styleDefault();
        }

        $ionicPlatform.registerBackButtonAction(function(e) {
            var confirmPopup = $ionicPopup.confirm({
                title: 'Confirm Exit',
                template: "Are you sure you want to exit"
            });
            confirmPopup.then(function(close) {
                if (close) {
                    // there is no back view, so close the app instead
                    ionic.Platform.exitApp();
                } else {
                    // otherwise do nothing
                    console.log("User canceled exit.");
                }
            });
            e.preventDefault();
            return false;
        }, 101); // 1 more priority than back button

        if (!window.localStorage.getItem("WallysphereIsFirstInstall")) {

            var deviceWidth = (window.innerWidth > 0) ? window.innerWidth : screen.width;
            var deviceHeight = (window.innerHeight > 0) ? window.innerHeight : screen.height;
            var devicePixelRatio = window.devicePixelRatio;

            window.plugin.notification.local.setScreenProperties({
                screenWidth: deviceWidth,
                screenHeight: deviceHeight,
                screenDensity: devicePixelRatio
            });

            window.localStorage.setItem("WallysphereIsFirstInstall", "true");
        } else {

        }

    });
})

.config(function($stateProvider, $urlRouterProvider) {
    $stateProvider
        .state('eventmenu', {
            url: "/event",
            abstract: true,
            templateUrl: "templates/event-menu.html"
        })
        .state('eventmenu.home', {
            url: "/home",
            views: {
                'menuContent': {
                    templateUrl: "templates/home.html",
                    controller: 'HomeCtrl'
                }
            }
        })
        .state('eventmenu.checkin', {
            url: "/settings",
            views: {
                'menuContent': {
                    templateUrl: "templates/settings.html",
                    controller: "SettingsCtrl"
                }
            }
        });

    $urlRouterProvider.otherwise("/event/home");
});
