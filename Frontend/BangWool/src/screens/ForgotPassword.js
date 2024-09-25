import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, Alert } from 'react-native';
import styles from './styles/ForgotStyle';

const ForgotPassword = ({ navigation }) => {
  const [email, setEmail] = useState('');

  const handlePasswordReset = () => {
    // 비밀번호 재설정 처리 로직 추가
    Alert.alert('비밀번호 재설정', `입력하신 이메일(${email})로 비밀번호 재설정 안내를 보냈습니다.`);
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>비밀번호 재설정</Text>
      <TextInput
        style={styles.input}
        placeholder="이메일 입력"
        value={email}
        onChangeText={setEmail}
        keyboardType="email-address"
      />
      <TouchableOpacity style={styles.button} onPress={handlePasswordReset}>
        <Text style={styles.buttonText}>재설정 링크 보내기</Text>
      </TouchableOpacity>
    </View>
  );
};

export default ForgotPassword;
