# :identification_card:QRcode_BusinessCard
## 🚀 소개
### 프로젝트 소개
**QRcard_Project**는 QR 코드를 활용한 **디지털 명함 기반 Android 연락처 앱**입니다.  
사용자별 고유 QR 코드를 생성해 스캔하면 이름, 이메일, 연락처 등의 정보를 **간편하고 빠르게 교환**할 수 있습니다.
내 정보 수정 시, 친구의 연락처 목록에서도 **자동으로 동기화**되어 최신 정보를 확인할 수 있습니다.
**Firebase Authentication**과 **Firestore**를 활용하여 사용자 및 친구 정보를 안전하게 관리합니다. 
이 앱은 기존의 종이 명함을 대체하여 **친환경적**으로 정보를 교환, 공유할 수 있습니다. 

### 주요 기능
- ✅ 회원가입 및 로그인
- ✅ 내 정보 조회 및 수정
- ✅ 친구 목록에서 내 정보 자동 동기화
- ✅ 고유 QR 코드 생성 및 스캔으로 친구 추가
- ✅ 친구 목록(연락처) 관리
- ✅ 친구 프로필 상세 조회

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
| **Back-end** | Firebase Authentication, Firestore, Java |
| **Front-end** |     XML layout, Activity + Fragment      |
| **Language** |                   Java                   |

## 🎯 기대 효과

(내용을 여기에 작성해주세요)


## 👥 팀원
| **Name** | **Position** |
|:--------:|:------------:|
| **이민기** |    PM, 개발    |
| **오금서** |      개발      |
| **남승희** |   장부MD, 개발   |
| **신현서** |   DB설계, 개발   |

