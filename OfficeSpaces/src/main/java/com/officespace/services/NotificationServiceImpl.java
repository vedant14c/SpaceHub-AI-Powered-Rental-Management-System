package com.officespace.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.officespace.daos.NotificationDao;
import com.officespace.entities.Notification;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class NotificationServiceImpl {
	@Autowired
	private NotificationDao notificationDao;

	public Notification addNotification(Notification notification) {

		notification.setIsRead(false);

		return notificationDao.save(notification);
	}

	public List<Notification> getNotificationsByUserId(int userId) {

		return notificationDao.findByUserId(userId);
	}

	public Notification getNotificationById(int id) {

		return notificationDao.findById(id).orElse(null);
	}

	public Notification markAsRead(int id) {

		Notification notification = notificationDao.findById(id).orElse(null);

		if (notification != null) {

			notification.setIsRead(true);

			return notificationDao.save(notification);
		}

		return null;
	}

}
