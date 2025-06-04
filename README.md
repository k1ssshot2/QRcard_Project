# :identification_card:QRcode_BusinessCard
## 🚀 소개
### 프로젝트 소개
**QRcard_Project**는 QR 코드를 활용한 **디지털 명함 기반 Android 연락처 앱**입니다.<br/><br/>
사용자별 고유 QR 코드를 생성해 스캔하면 이름, 이메일, 연락처 등의 **정보를 간편하고 빠르게 교환**할 수 있습니다.<br/><br/>
내 정보 수정 시, 친구의 연락처 목록에서도 **자동으로 동기화**되어 최신 정보를 확인할 수 있습니다.<br/><br/>
**Firebase Authentication**과 **Firestore**를 활용하여 사용자 및 친구 정보를 안전하게 관리합니다.<br/><br/>
이 앱은 기존의 종이 명함을 대체하여 **친환경적**으로 정보를 교환, 공유할 수 있습니다.<br/><br/>
***

### 주요 기능
- ✅ 회원가입 및 로그인
- ✅ 내 정보 조회 및 수정
- ✅ 고유 QR 코드 생성 및 스캔으로 친구 추가
- ✅ 친구 목록(연락처) 관리
- ✅ 친구 프로필 상세 조회
- ✅ 친구 목록에서 내 정보 자동 동기화
***

### 프로젝트 구조
📦com.example.qrcardproject  
├── NaviActivity.java (main)  
├── StartScreenActivity.java  
├── LoginScreenActivity.java  
├── RegisterActivity.java (Signup)  
├── FriendProfileFragment.java  
├── fragments  
│   ├── HomeFragment.java  
│   │   └──AddScanFragment.java  
│   ├── ContactsFragment.java  
│   │   └── FriendProfileFragment.java  
│   ├── MyInfoFragment.java  
│     
├── models  
│   └── Friend.java  
├── adapters  
│   └── FriendAdapter.java  
├── res  
│   ├── layout/  
│   └── drawable/  
└── AndroidManifest.xml  



## 🛠️ 기술 스택
| **Category** |                **Stack**                 |
|:------------:|:----------------------------------------:|
| **Back-end** | <img src="https://img.shields.io/badge/firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=white"> ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white) |
| **Front-end** |     ![XML Layout](https://img.shields.io/badge/XML%20Layout-4285F4?style=for-the-badge&logo=android&logoColor=white), ![Activity](https://img.shields.io/badge/Activity-34A853?style=for-the-badge&logo=android&logoColor=white) + ![Fragment](https://img.shields.io/badge/Fragment-34A853?style=for-the-badge&logo=android&logoColor=white)    |
| **Language** |                   ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=java&logoColor=white)                   |


## 🎯 기대 효과

이 앱은 생활 분야에 있어서 **QR code를 통해 서로의 연락처를 간단하게 교환할 수 있다는 장점**이 있습니다.<br/><br/>
추가로 개인의 편의를 넘어서 **조직 내 협업과 정보 공유 측면**에서도 큰 가치를 지니고 있는데, 팀 내에서 연락처 정보를 손쉽게<br/><br/>
공유하고 신속하게 소통함으로써 업무 연계가 한층 원활해지고, 네트워크 형성과 협업 능력을 강화하는 데 실질적으로 기여를<br/><br/>
할 수 있습니다. 사용자 경험(UX) 측면에서도 기존의 불편함을 완전히 해소하고, **직관적이고 편리한 인터페이스를 제공**하는 데<br/><br/>
주력하여 실제 사용자들이 쉽게 적응하고 적극적으로 활용할 수 있는 앱을 구현하는 데 주안점을 두었습니다.<br/><br/>
나의 정보 변경 시 상대한테서도 **자신의 변경된 정보가 자동으로 동기화**되어 정보의 최신화가 간편하다는 장점을 가집니다.<br/><br/>
환경 보호 차원에서는 **디지털 명함이기 때문에 불필요한 종이 자원을 줄여** 친환경적인 이점을 가져다줍니다.<br/><br/>


## 👥 팀원
| **Name** | **Position** |
|:--------:|:------------:|
| **이민기** |    PM, 개발    |
| **오금서** |      개발      |
| **남승희** |   장부MD, 개발   |
| **신현서** |   DB설계, 개발   |

