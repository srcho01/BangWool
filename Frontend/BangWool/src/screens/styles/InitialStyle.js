import { StyleSheet } from 'react-native';

const styles = StyleSheet.create({
    container: {
      flex: 1,
      justifyContent: 'center',
      alignItems: 'center',
      backgroundColor: '#fff',
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
    snsButtonContainer: {
      flexDirection: 'row',
      justifyContent: 'space-between',
      marginBottom: 10,
    },
    snsButton: {
      marginTop: 20,
      flexDirection: 'row',
      alignItems: 'center',
      justifyContent: 'center',
      backgroundColor: '#3b5998',
      paddingVertical: 5,
      borderRadius: 5,
      width: '48%', 
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
      width: '100%',
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
      width: '100%', // 회원가입 버튼도 가로 전체 차지
    },
    signUpButtonText: {
      color: '#fff',
      fontSize: 16,
    },
  });
export default styles;
