# 🚀 Play Store Publishing Checklist

## ✅ Pre-Launch Tasks

### 1. Generate Signing Key (One-time)
```bash
keytool -genkey -v -keystore skyzonebd-release-key.jks -alias skyzonebd -keyalg RSA -keysize 2048 -validity 10000
```

**✅ DONE:** [ ]  
**Keystore Password:** `____________` (SAVE SECURELY!)  
**Key Password:** `____________` (SAVE SECURELY!)  
**File Location:** `skyzonebd-release-key.jks` (in project root, NOT committed to Git)

### 2. Update Version
In `app/build.gradle.kts`:
- [ ] `versionCode = 1` (increment for each release)
- [ ] `versionName = "1.0.0"` (semantic versioning)

### 3. Set Environment Variables
```powershell
# Windows PowerShell (set permanently)
[System.Environment]::SetEnvironmentVariable('KEYSTORE_PASSWORD', 'your_password', 'User')
[System.Environment]::SetEnvironmentVariable('KEY_PASSWORD', 'your_password', 'User')

# Or temporarily for current session
$env:KEYSTORE_PASSWORD = "your_password"
$env:KEY_PASSWORD = "your_password"
```
**✅ DONE:** [ ]

### 4. Build Release AAB
```bash
./gradlew clean
./gradlew bundleRelease
```
**✅ AAB Created:** [ ]  
**Location:** `app/build/outputs/bundle/release/app-release.aab`  
**Size:** `________ MB`

### 5. Test Release Build
- [ ] Install on physical device
- [ ] Test all core features
- [ ] Check no crashes
- [ ] Verify API connectivity
- [ ] Test payments (if applicable)

---

## 🎨 Graphics Assets

### App Icon
- [ ] **512x512 px PNG**
- [ ] 32-bit with alpha
- [ ] No rounded corners (Android adds them)
- [ ] Clear and recognizable
- **File:** `ic_launcher_512.png`

### Feature Graphic
- [ ] **1024x500 px JPG/PNG**
- [ ] Eye-catching banner
- [ ] Brand colors
- [ ] Text overlay: "B2B & B2C Marketplace"
- **File:** `feature_graphic.png`

### Screenshots (Phone)
Need at least 2, recommended 8:
- [ ] **1080x1920 px or 1440x2560 px**
- [ ] Screenshot 1: Home screen
- [ ] Screenshot 2: Product listing
- [ ] Screenshot 3: Product details
- [ ] Screenshot 4: Shopping cart
- [ ] Screenshot 5: Order tracking
- [ ] Screenshot 6: User profile
- [ ] Screenshot 7: B2B features
- [ ] Screenshot 8: Checkout process

Add text overlays highlighting features!

### Screenshots (Tablet) - Optional
- [ ] **1080x1920 px (landscape)**
- [ ] At least 2 screenshots

### Promo Video - Optional but Recommended
- [ ] **30-120 seconds**
- [ ] YouTube URL
- [ ] Shows app walkthrough
- [ ] Highlights key features

---

## 📝 Store Listing Content

### App Title
- [ ] **Max 50 characters**
- [ ] Example: `SkyzoneBD - B2B & B2C Shopping App`
- [ ] Includes main keyword

### Short Description
- [ ] **Max 80 characters**
- [ ] Example: `Shop wholesale & retail. Best B2B marketplace in Bangladesh. Bulk discounts!`

### Full Description
- [ ] **Max 4000 characters**
- [ ] Keywords in first 250 characters
- [ ] Features highlighted with emojis
- [ ] Clear value proposition
- [ ] Call-to-action at end
- [ ] See `PLAY_STORE_PUBLISHING_GUIDE.md` for template

### Categories
- [ ] **Primary:** Shopping
- [ ] **Secondary:** Business
- [ ] **Tags:** wholesale, B2B, marketplace, shopping, e-commerce

### Contact Details
- [ ] Email: `support@skyzonebd.com` or your email
- [ ] Website: `https://skyzonebd.vercel.app`
- [ ] Privacy Policy URL: `____________`

---

## 🔒 Legal & Compliance

### Privacy Policy
- [ ] **REQUIRED!** Must be hosted online
- [ ] URL: `____________`
- [ ] Covers: data collection, usage, third-party services, user rights
- [ ] Tool: https://www.privacypolicygenerator.info/

### Data Safety Form
- [ ] Describe data collection practices
- [ ] Encryption in transit
- [ ] User data deletion option
- [ ] No data shared with third parties (unless you do)

### Content Rating
- [ ] Complete questionnaire
- [ ] Target age: **Everyone (3+)**
- [ ] No violence, gambling, or mature content

### App Access
- [ ] Provide test account if login required
- [ ] Username: `____________`
- [ ] Password: `____________`

---

## 🏪 Play Console Setup

### Developer Account
- [ ] Created at https://play.google.com/console
- [ ] Paid $25 registration fee
- [ ] Identity verified
- [ ] Accepted Developer Distribution Agreement

### Create App
- [ ] App name: `SkyzoneBD - B2B & B2C Marketplace`
- [ ] Default language: English (US)
- [ ] App/Game: App
- [ ] Free/Paid: Free
- [ ] All declarations completed

### Countries
- [ ] Bangladesh (primary)
- [ ] India
- [ ] Pakistan
- [ ] Other South Asian countries
- [ ] Global (if you want)

---

## 🚀 Pre-Submission Checklist

### Technical
- [x] Signing key generated and secured
- [x] Build.gradle updated with signing config
- [x] ProGuard rules optimized
- [ ] Release AAB built and tested
- [ ] No hardcoded API keys in code
- [ ] All debug logs removed
- [ ] App tested on Android 7.0+

### Content
- [ ] App icon (512x512)
- [ ] Feature graphic (1024x500)
- [ ] At least 2 phone screenshots
- [ ] App title optimized
- [ ] Short description
- [ ] Full description with keywords
- [ ] Privacy policy URL
- [ ] Contact email

### Store Listing
- [ ] Main Store Listing completed
- [ ] Content rating completed
- [ ] Data safety form filled
- [ ] Pricing & distribution set
- [ ] App access info provided (if needed)
- [ ] Target audience selected

---

## 📱 Submission Process

### 1. Upload AAB
- [ ] Go to "Production" > "Create new release"
- [ ] Upload `app-release.aab`
- [ ] Wait for processing (5-10 minutes)

### 2. Release Notes
```
🎉 Initial Release - Version 1.0.0

Welcome to SkyzoneBD!

✨ Features:
• Browse products by category
• B2B wholesale and B2C retail shopping
• Bulk discounts for businesses
• Secure checkout
• Order tracking
• User account management

Download now and start shopping!
```

### 3. Review
- [ ] Review all info one last time
- [ ] Click "Start rollout to Production"
- [ ] Wait 1-3 days for Google review

---

## 📊 Post-Launch (First Week)

### Day 1
- [ ] App goes live!
- [ ] Share on social media
- [ ] Email announcement to users
- [ ] Monitor crash reports

### Day 1-7
- [ ] Respond to all reviews
- [ ] Monitor Play Console dashboard
- [ ] Check for crashes/bugs
- [ ] Track download metrics
- [ ] Start marketing campaigns

### Week 2-4
- [ ] Analyze user feedback
- [ ] Fix critical issues
- [ ] Prepare first update
- [ ] Optimize ASO based on data

---

## 🎯 ASO Optimization (Ongoing)

### Keywords to Track
- [ ] B2B marketplace Bangladesh
- [ ] Wholesale shopping app
- [ ] Bulk buying Bangladesh
- [ ] Online marketplace
- [ ] E-commerce Bangladesh

### Monthly Tasks
- [ ] Update description with new keywords
- [ ] Refresh screenshots
- [ ] Analyze competitor apps
- [ ] Update release notes with features
- [ ] Run A/B tests on graphics

### Growth Targets
- **Week 1:** 50-100 downloads
- **Month 1:** 500-1000 downloads
- **Month 3:** 5000+ downloads
- **Target Rating:** 4.5+ stars

---

## 💰 Marketing Budget (Optional)

### Free Marketing
- [ ] Social media posts
- [ ] Email to existing users
- [ ] WhatsApp business groups
- [ ] Cross-promote on website
- [ ] Content marketing (blogs)

### Paid Marketing (Optional)
- [ ] Google UAC: $5-10/day
- [ ] Facebook Ads: $5-10/day
- [ ] Instagram influencers
- [ ] YouTube reviews

**Total Budget to Start:** Just $25 (Play Console fee)!

---

## 🆘 Emergency Contacts

- **Play Console Support:** https://support.google.com/googleplay/android-developer
- **Developer Policy:** https://play.google.com/about/developer-content-policy/
- **Stack Overflow:** [android-publishing] tag

---

## 📅 Timeline

| Phase | Duration | Status |
|-------|----------|--------|
| Generate signing key | 10 mins | [ ] |
| Create graphics | 1-2 days | [ ] |
| Write store listing | 2-4 hours | [ ] |
| Build release AAB | 30 mins | [ ] |
| Set up Play Console | 1 hour | [ ] |
| Submit for review | 5 mins | [ ] |
| **Google Review** | **1-3 days** | [ ] |
| **LIVE!** | 🎉 | [ ] |

---

## ✅ Final Pre-Submit Checklist

Right before clicking "Submit":

- [ ] AAB uploaded and processed successfully
- [ ] Version code/name correct
- [ ] All screenshots look good
- [ ] Description has no typos
- [ ] Privacy policy URL works
- [ ] Contact email is correct
- [ ] Countries selected
- [ ] Pricing set (Free)
- [ ] Content rating completed
- [ ] Data safety submitted
- [ ] Release notes written
- [ ] Deep breath taken 😊

---

**🚀 READY TO LAUNCH!**

Once submitted, your app will be reviewed within 1-3 days. You'll receive an email when it's live!

Good luck! 🎉
