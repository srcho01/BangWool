import React from 'react';
import { View, Text, Image, TouchableOpacity, StyleSheet } from 'react-native';
import Icon from 'react-native-vector-icons/FontAwesome';

const InitialScreen = ({ navigation }) => {
  return (
    <View style={styles.container}>
      <View style={styles.logoContainer}>
        <Image source={require('../../assets/images/default.png')} style={styles.logo} />
        <Text style={styles.description}>내 화장품의 유통기한을 기록하고 위치를 저장해요</Text>
      </View>
      <Text style={styles.description}>간편 SNS 로그인</Text>
      <View style={styles.buttonContainer}>
        <View style={styles.viewcontainer}>
        <TouchableOpacity style={styles.snsButton}>
          <Icon name="comment" size={24} color="#fff" />
          <Text style={styles.snsButtonText}>카카오톡</Text>
        </TouchableOpacity>
        
        <TouchableOpacity style={styles.snsButton}>
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

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#fff',
  },
  viewcontainer: {
    flexDirection: 'row', // 가로 배치
    //justifyContent: 'space-between', // 버튼들 사이의 간격 조정
  },
  logoContainer: {
    alignItems: 'center',
    marginBottom: 50,
  },
  logo: {
    width: 100,
    height: 100,
    marginBottom: 20,
  },
  description: {
    fontSize: 16,
    textAlign: 'center',
    paddingHorizontal: 20,
  },
  buttonContainer: {
    width: '80%',
  },
  snsButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#3b5998',
    paddingVertical: 10,
    marginVertical: 5,
    borderRadius: 5,
  },
  snsButtonText: {
    color: '#fff',
    marginLeft: 10,
    fontSize: 16,
  },
  emailButton: {
    backgroundColor: '#888',
    paddingVertical: 10,
    marginVertical: 5,
    borderRadius: 5,
    alignItems: 'center',
  },
  emailButtonText: {
    color: '#fff',
    fontSize: 16,
  },
  signUpButton: {
    backgroundColor: '#555',
    paddingVertical: 10,
    marginVertical: 5,
    borderRadius: 5,
    alignItems: 'center',
  },
  signUpButtonText: {
    color: '#fff',
    fontSize: 16,
  },
});

export default InitialScreen;
