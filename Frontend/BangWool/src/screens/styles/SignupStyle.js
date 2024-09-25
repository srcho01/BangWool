import { StyleSheet } from 'react-native';

const styles = StyleSheet.create({
  background: {
    flex: 1,
    justifyContent: 'center',
  },
  container: {
    justifyContent: 'center', 
    paddingHorizontal: 16,
    paddingVertical: 20,
    marginHorizontal: 16,
    borderRadius: 10,
    backgroundColor: 'rgba(232, 232, 232, 0.8)',
  },
  com: {
    position: 'absolute',
    top: 16,
    right: 16, 
    fontSize: 24,
    fontFamily: 'KCC-Hanbit',
  },
  title: {
    fontSize: 24,
    marginBottom: 16,
    textAlign: 'left', 
    marginLeft: 20, 
  },
  inputContainer: {
    marginBottom: 12,
  },
  inputLabel: {
    fontSize: 16,
    marginBottom: 4,
  },
  nicknameContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  input: {
    height: 40,
    borderColor: 'gray',
    paddingHorizontal: 8,
    borderRadius: 8,
    backgroundColor: 'rgb(253, 253, 253)',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  rowContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  emailInput: {
    flex: 1,  // 버튼 옆에 위치하도록 가로 공간 차지
    marginRight: 8,  // 버튼과의 간격
  },
  codeInput: {
    flex: 1,  // 버튼 옆에 위치하도록 가로 공간 차지
    marginRight: 8,  // 버튼과의 간격
  },
  smallButton: {
    backgroundColor: 'rgb(91, 115, 133)',
    paddingVertical: 6,  // 버튼 크기 줄임
    paddingHorizontal: 10,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  smallButtonText: {
    color: 'white',
    fontSize: 12,  // 텍스트 크기 줄임
  },

  checkboxContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 12,
  },
  checkButton: {
    backgroundColor: 'rgb(91, 115, 133)',
    padding: 10,
    borderRadius: 8,
    marginLeft: 8,
    alignItems: 'center',
    justifyContent: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  checkButtonText: {
    color: 'white',
    fontSize: 14,
  },
  errorText: {
    color: 'red',
    marginTop: 4,
  },
  successText: {
    color: 'green',
    marginTop: 4,
  },
  checkbox: {
    width: 20,
    height: 20,
    borderWidth: 1,
    borderRadius: 8,
    borderColor: 'gray',
    backgroundColor: 'rgb(91, 115, 133)',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 8,
  },
  checkboxLabel: {
    fontSize: 14,
  },
  button: {
    backgroundColor: 'rgb(91, 115, 133)',
    padding: 10,
    borderRadius: 8,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5,
  },
  buttonText: {
    color: 'white',
    fontSize: 16,
  },
  footer: {
    flexDirection: 'column',
    alignItems: 'center',
    marginTop: 12,
  },
  link: {
    color: 'blue',
  },
});

export default styles;
