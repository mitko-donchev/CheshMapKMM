import Foundation
import UIKit
import GoogleMaps

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        // Load the Google maps API key from the AppSecrets.plist file
        let filePath = Bundle.main.path(forResource: "AppSecrets", ofType: "plist")!
        let plist = NSDictionary(contentsOfFile: filePath)!
        let googleMapsApiKey = plist["GOOGLE_MAPS_API_KEY"] as! String

        GMSServices.provideAPIKey(googleMapsApiKey)
        return true
    }
}
