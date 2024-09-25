import React, { useEffect } from 'react';
import { View, Text, Image, TouchableOpacity } from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome';
import { GoogleSignin, statusCodes } from '@react-native-google-signin/google-signin';
import styles from './styles/InitialStyle';
import { serverUrl, AndroidId } from '@env';

const InitialScreen = ({ navigation }) => {
  useEffect(() => {
    GoogleSignin.configure({
      AndroidClientId: AndroidId, 
    });
  }, []);

  // 구글 회원가입 및 로그인 처리
  const signInWithGoogle = async () => {
    try {
      await GoogleSignin.hasPlayServices();
      const userInfo = await GoogleSignin.signIn();
  
      console.log('구글 로그인 성공:', userInfo); // userInfo 객체 로그 출력
  
      // userInfo.user에서 user 정보 추출
      const user = userInfo.data.user; // user 객체를 추출합니다.
  
      if (!user) {
        console.log("사용자 정보를 가져올 수 없습니다.");
        return;
      }
  
      const { email, givenName, familyName, id } = user; 
  
      // 회원가입 시도
      const signupResponse = await fetch(serverUrl + 'auth/signup/oauth', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: email,
          name: `${familyName}${givenName}`,
          nickname: givenName,
          birth: '2000-01-01',
          googleId: id,
        }),
      });
  
      if (signupResponse.ok) {
        console.log('회원가입 성공');
        navigation.navigate('Home'); // 홈 화면으로 이동
      } else {
        const responseData = await signupResponse.json();
        console.log('회원가입 실패', responseData);
  
        // 사용자 이미 등록된 경우 로그인 시도
        if (responseData.code === 400 && responseData.message === "User is already registered.") {
          console.log('사용자가 이미 존재합니다. 로그인 시도 중...');
          await loginWithGoogle(email, id); // email과 id를 전달하여 로그인 진행
        }
      }
    } catch (error) {
      console.log('구글 로그인 오류:', error); // 오류 발생 시 로그 출력
      handleGoogleSignInError(error);
    }
  };
  
  // 구글 로그인 처리
  const loginWithGoogle = async (email, googleId) => {
    try {
      // email과 googleId가 올바르게 전달되었는지 확인
      if (!email || !googleId) {
        console.error("로그인에 필요한 정보가 부족합니다.", { email, googleId });
        return;
      }

      const response = await fetch(serverUrl + 'login/oauth', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: email,
          googleId: googleId,
        }),
      });

      const data = await response.json();
      if (response.ok) {
        console.log('로그인 성공:', data);
        navigation.navigate('Home'); // 홈 화면으로 이동
      } else {
        console.log('로그인 실패:', data);
      }
    } catch (error) {
      console.error('로그인 오류:', error);
    }
  };

  // 구글 로그인 에러 처리 함수
  const handleGoogleSignInError = (error) => {
    if (error.code === statusCodes.SIGN_IN_CANCELLED) {
      console.log('로그인 취소됨');
    } else if (error.code === statusCodes.IN_PROGRESS) {
      console.log('로그인 진행 중');
    } else if (error.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE) {
      console.log('Play Services 사용 불가');
    } else {
      console.log('기타 에러:', error);
    }
  };

  return (
    <View style={styles.container}>
      <View style={styles.logoContainer}>
        <Image source={require('../../assets/images/default.png')} style={styles.logo} />
        <Text style={styles.description}>내 화장품의 유통기한을 기록하고 위치를 저장해요</Text>
      </View>
      <Text style={styles.description}>간편 SNS 로그인</Text>
      <View style={styles.buttonContainer}>
        <View style={styles.snsButtonContainer}>
          <TouchableOpacity style={styles.snsButton}>
            <Icon name="comment" size={24} color="#fff" />
            <Text style={styles.snsButtonText}>카카오톡</Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.snsButton} onPress={signInWithGoogle}>
            <Icon name="google" size={24} color="#fff" />
            <Text style={styles.snsButtonText}>구글</Text>
          </TouchableOpacity>
        </View>

        <TouchableOpacity style={styles.emailButton} onPress={() => navigation.navigate('Login')}>
          <Text style={styles.emailButtonText}>이메일 로그인</Text>
        </TouchableOpacity>

        <TouchableOpacity style={styles.signUpButton} onPress={() => navigation.navigate('Signup')}>
          <Text style={styles.signUpButtonText}>회원가입</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

export default InitialScreen;
