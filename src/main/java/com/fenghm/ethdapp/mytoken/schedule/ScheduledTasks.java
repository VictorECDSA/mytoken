package com.fenghm.ethdapp.mytoken.schedule;

import java.io.File;
import java.math.BigDecimal;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import com.fenghm.ethdapp.mytoken.common.Constant;

@Component
public class ScheduledTasks {

	@Scheduled(cron = "0 0 0 * * ?")
	public void allocateEth() {
		System.out.println("allocateEth begin");
		for (File file : Constant.accountFolder.toFile().listFiles()) {
			System.out.println("found file: " + file);
			if (!file.isFile()) {
				continue;
			}
			String accountId = file.getName();
			if (accountId.equals(Constant.systemAccountId)) {
				continue;
			}
			System.out.println("allocate eth to " + accountId);
			try {
				Transfer.sendFunds(Constant.admin, Constant.systemCredentials, accountId, BigDecimal.valueOf(1L),
						Convert.Unit.ETHER).send();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

		}
		System.out.println("allocateEth finished");
	}

}
