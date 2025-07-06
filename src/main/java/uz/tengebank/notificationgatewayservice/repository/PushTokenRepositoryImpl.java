package uz.tengebank.notificationgatewayservice.repository;

import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PushTokenRepositoryImpl implements PushTokenRepository {

  @Override
  public List<String> findTokensByPhone(String phone) {
    return List.of(
        "qazwsxedcrfvtgbyhndasfdsfdsfgdsgdsgsdfgdfsgdfsgdfgersdsdasfsdgdfhgdh",
        "aewrsrtdsfgdfgdsgdfhgdfhgfghfghdfgdftdfthyfthdfthyfthfthytjdssredzsf"
    );
  }
}
